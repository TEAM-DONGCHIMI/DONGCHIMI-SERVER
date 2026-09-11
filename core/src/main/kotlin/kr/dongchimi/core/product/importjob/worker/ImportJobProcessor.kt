package kr.dongchimi.core.product.importjob.worker

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import kr.dongchimi.core.admin.DefaultProductThumbnailRepository
import kr.dongchimi.core.monitoring.ErrorNotificationDispatcher
import kr.dongchimi.core.product.DraftFailReasonResolver
import kr.dongchimi.core.product.DraftStatus
import kr.dongchimi.core.product.PreparedProduct
import kr.dongchimi.core.product.ProductCategory
import kr.dongchimi.core.product.importjob.ExactThumbnailMatcher
import kr.dongchimi.core.product.importjob.ExcelProductParser
import kr.dongchimi.core.product.importjob.ImportCanceledException
import kr.dongchimi.core.product.importjob.ImportJob
import kr.dongchimi.core.product.importjob.ImportJobCancelSignal
import kr.dongchimi.core.product.importjob.ImportJobErrorCode
import kr.dongchimi.core.product.importjob.ImportJobEvent
import kr.dongchimi.core.product.importjob.ImportJobEventChannel
import kr.dongchimi.core.product.importjob.ImportJobFinisher
import kr.dongchimi.core.product.importjob.ImportJobProgressStore
import kr.dongchimi.core.product.importjob.ImportJobProperties
import kr.dongchimi.core.product.importjob.ImportJobRepository
import kr.dongchimi.core.product.importjob.ImportJobResult
import kr.dongchimi.core.product.importjob.ImportStep
import kr.dongchimi.core.product.importjob.ParsedProductRow
import kr.dongchimi.core.product.importjob.ProductCategoryClassifier
import kr.dongchimi.core.product.importjob.ProductCategoryClassifyItem
import kr.dongchimi.core.product.importjob.ProductImageMatchItem
import kr.dongchimi.core.product.importjob.ProductImageMatcher
import kr.dongchimi.core.upload.StorageClient
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

/**
 * job 하나를 처음부터 끝까지 실행한다. 리스 갱신 코루틴을 병행하고, 각 단계 시작 시점에
 * 취소 신호를 확인한다(§3-5). NAME_EXTRACTION/PRICE_EXTRACTION은 실제로 한 번의 시트
 * 순회라 파싱은 NAME_EXTRACTION에서 1회만 하고 PRICE_EXTRACTION은 이벤트만 낸다.
 */
@Component
class ImportJobProcessor(
    private val importJobRepository: ImportJobRepository,
    private val storageClient: StorageClient,
    private val excelProductParser: ExcelProductParser,
    private val productCategoryClassifier: ProductCategoryClassifier,
    private val productImageMatcher: ProductImageMatcher,
    private val exactThumbnailMatcher: ExactThumbnailMatcher,
    private val defaultProductThumbnailRepository: DefaultProductThumbnailRepository,
    private val importJobProgressStore: ImportJobProgressStore,
    private val importJobEventChannel: ImportJobEventChannel,
    private val importJobCancelSignal: ImportJobCancelSignal,
    private val importJobFinisher: ImportJobFinisher,
    private val draftFailReasonResolver: DraftFailReasonResolver,
    private val errorNotificationDispatcher: ErrorNotificationDispatcher,
    private val properties: ImportJobProperties,
) : ImportJobRunner {
    override suspend fun run(
        job: ImportJob,
        instanceId: String,
    ) = coroutineScope {
        val leaseRenewal =
            launch {
                while (isActive) {
                    delay(properties.lease.dividedBy(3).toMillis())
                    importJobRepository.renewLease(job.jobId, instanceId, properties.lease)
                }
            }

        try {
            runPipeline(job)
        } catch (e: ImportCanceledException) {
            if (importJobFinisher.cancel(job.jobId)) {
                importJobEventChannel.publish(ImportJobEvent.Canceled(job.jobId))
            }
        } catch (e: Exception) {
            logger.error(e) { "엑셀 분석 실패: jobId=${job.jobId}" }
            if (importJobFinisher.fail(job.jobId, ImportJobErrorCode.ANALYSIS_FAILED.name)) {
                importJobEventChannel.publish(
                    ImportJobEvent.Failed(
                        jobId = job.jobId,
                        errorCode = ImportJobErrorCode.ANALYSIS_FAILED.name,
                        message = ImportJobErrorCode.ANALYSIS_FAILED.message,
                    ),
                )
            }
            errorNotificationDispatcher.dispatchJob(e, job.jobId)
        } finally {
            leaseRenewal.cancel()
        }
    }

    private suspend fun runPipeline(job: ImportJob) {
        val tracker = ImportProgressTracker(job.jobId, importJobProgressStore, importJobEventChannel, importJobCancelSignal)

        var bytes = ByteArray(0)
        var rows: List<ParsedProductRow> = emptyList()

        tracker.step(ImportStep.FILE_UPLOAD) { bytes = withContext(Dispatchers.IO) { storageClient.download(job.excelObjectKey) } }
        tracker.step(ImportStep.NAME_EXTRACTION) { rows = withContext(Dispatchers.Default) { excelProductParser.parse(bytes) } }
        tracker.step(ImportStep.PRICE_EXTRACTION) { /* NAME_EXTRACTION에서 이미 파싱된 rows를 그대로 쓴다 */ }

        val categories =
            tracker.step(ImportStep.CATEGORY_CLASSIFICATION) {
                classifyAll(rows)
            }
        val thumbnails =
            tracker.step(ImportStep.IMAGE_MATCHING) {
                matchAll(rows, categories)
            }

        val drafts = buildDrafts(job.marketId, rows, categories, thumbnails)
        val result =
            ImportJobResult(
                totalCount = drafts.size,
                successCount = drafts.count { it.draftStatus == DraftStatus.SUCCESS },
                failCount = drafts.count { it.draftStatus == DraftStatus.FAIL },
            )

        if (importJobFinisher.complete(job, drafts, result)) {
            importJobEventChannel.publish(
                ImportJobEvent.Completed(
                    jobId = job.jobId,
                    totalCount = requireNotNull(result.totalCount),
                    successCount = requireNotNull(result.successCount),
                    failCount = requireNotNull(result.failCount),
                ),
            )
        }
    }

    /**
     * 항목을 [ImportJobProperties.aiBatchSize] 단위 청크로 묶어 AI를 배치 호출하고,
     * 청크 호출은 [ImportJobProperties.aiConcurrency]로 동시 실행 상한을 건다. 결과는 rows와 같은 순서로 정렬된다.
     */
    private suspend fun classifyAll(rows: List<ParsedProductRow>): List<ProductCategory?> {
        val items =
            rows.withIndex().mapNotNull { (index, row) ->
                row.name?.let { ProductCategoryClassifyItem(index, it) }
            }

        val results = mutableMapOf<Int, ProductCategory?>()
        coroutineScope {
            val semaphore = Semaphore(properties.aiConcurrency)
            items
                .chunked(properties.aiBatchSize)
                .map { chunk -> async { semaphore.withPermit { productCategoryClassifier.classify(chunk) } } }
                .awaitAll()
                .forEach { results += it }
        }
        return rows.indices.map { results[it] }
    }

    /**
     * 분류 성공(category != null)한 행만 대상으로 한다. 먼저 이름이 후보 썸네일과 정규화 후 정확히
     * 일치하는 행을 [ExactThumbnailMatcher]로 확정하고, 나머지만 category와 무관하게 batch-size로
     * 청크를 나눠 AI에 배치 호출한다.
     */
    private suspend fun matchAll(
        rows: List<ParsedProductRow>,
        categories: List<ProductCategory?>,
    ): List<String?> {
        val items =
            rows.indices.mapNotNull { i ->
                val name = rows[i].name ?: return@mapNotNull null
                val category = categories[i] ?: return@mapNotNull null // 분류 실패 행은 매칭 자체를 시도하지 않는다
                ProductImageMatchItem(i, name, category)
            }

        val presentCategories = items.map { it.category }.toSet()
        val candidates =
            if (presentCategories.isEmpty()) {
                emptyList()
            } else {
                withContext(Dispatchers.IO) { defaultProductThumbnailRepository.findAllByCategoryIn(presentCategories) }
            }

        val exactMatched = exactThumbnailMatcher.match(items, candidates)
        val remaining = items.filter { it.id !in exactMatched }

        val results = mutableMapOf<Int, String?>()
        results += exactMatched
        coroutineScope {
            val semaphore = Semaphore(properties.aiConcurrency)
            remaining
                .chunked(properties.aiBatchSize)
                .map { chunk -> async { semaphore.withPermit { productImageMatcher.match(chunk) } } }
                .awaitAll()
                .forEach { results += it }
        }
        return rows.indices.map { results[it] }
    }

    private fun buildDrafts(
        marketId: Long,
        rows: List<ParsedProductRow>,
        categories: List<ProductCategory?>,
        thumbnails: List<String?>,
    ): List<PreparedProduct> =
        rows.indices.map { i ->
            val row = rows[i]
            val category = categories[i]
            val thumbnailUrl = thumbnails[i]
            val failReason = draftFailReasonResolver.resolve(row, category, thumbnailUrl)

            PreparedProduct(
                marketId = marketId,
                name = row.name,
                thumbnailUrl = thumbnailUrl,
                price = row.price,
                category = category,
                promotionalPhrase = row.promotionalPhrase,
                discountPeriod = row.discountPeriod,
                draftStatus = if (failReason == null) DraftStatus.SUCCESS else DraftStatus.FAIL,
                failReason = failReason,
            )
        }
}
