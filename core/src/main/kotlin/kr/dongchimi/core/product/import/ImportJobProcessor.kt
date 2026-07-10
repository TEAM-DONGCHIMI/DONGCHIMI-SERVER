package kr.dongchimi.core.product.import

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import kr.dongchimi.core.product.DraftFailReasonResolver
import kr.dongchimi.core.product.DraftStatus
import kr.dongchimi.core.product.PreparedProduct
import kr.dongchimi.core.product.ProductCategory
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
    private val importJobProgressStore: ImportJobProgressStore,
    private val importJobEventChannel: ImportJobEventChannel,
    private val importJobCancelSignal: ImportJobCancelSignal,
    private val importJobFinisher: ImportJobFinisher,
    private val draftFailReasonResolver: DraftFailReasonResolver,
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
        } finally {
            leaseRenewal.cancel()
        }
    }

    private suspend fun runPipeline(job: ImportJob) {
        val stepStatuses = ImportStep.entries.associateWithTo(linkedMapOf()) { ImportStepStatus.PENDING }

        suspend fun emitProgress(
            currentStep: ImportStep,
            itemRatio: Double,
        ) {
            val progress = computeProgress(currentStep, itemRatio)
            val snapshot =
                ImportJobProgress(
                    jobId = job.jobId,
                    progress = progress,
                    // 최근 진행 속도 기반 추정은 §9 오픈 이슈 — 실제 AI 붙을 때 다시 손본다.
                    remainingSeconds = null,
                    currentStep = currentStep,
                    steps = ImportStep.entries.map { ImportStepProgress(it, stepStatuses.getValue(it)) },
                )
            importJobProgressStore.save(snapshot)
            importJobEventChannel.publish(ImportJobEvent.Progress(snapshot))
        }

        suspend fun <T> step(
            step: ImportStep,
            block: suspend () -> T,
        ): T {
            currentCoroutineContext().ensureActive()
            if (importJobCancelSignal.isRequested(job.jobId)) throw ImportCanceledException()

            stepStatuses[step] = ImportStepStatus.IN_PROGRESS
            emitProgress(step, 0.0)

            val result =
                try {
                    block()
                } catch (e: ImportCanceledException) {
                    throw e
                } catch (e: Exception) {
                    stepStatuses[step] = ImportStepStatus.FAILED
                    throw e
                }

            stepStatuses[step] = ImportStepStatus.COMPLETED
            emitProgress(step, 1.0)
            return result
        }

        var bytes = ByteArray(0)
        var rows: List<ParsedProductRow> = emptyList()

        step(ImportStep.FILE_UPLOAD) { bytes = withContext(Dispatchers.IO) { storageClient.download(job.excelObjectKey) } }
        step(ImportStep.NAME_EXTRACTION) { rows = withContext(Dispatchers.Default) { excelProductParser.parse(bytes) } }
        step(ImportStep.PRICE_EXTRACTION) { /* NAME_EXTRACTION에서 이미 파싱된 rows를 그대로 쓴다 */ }

        val categories =
            step(ImportStep.CATEGORY_CLASSIFICATION) {
                classifyAll(rows)
            }
        val thumbnails =
            step(ImportStep.IMAGE_MATCHING) {
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

    /** 항목 수만큼 AI를 동시에 부르되 [ImportJobProperties.aiConcurrency]로 상한을 건다. 결과는 rows와 같은 순서로 정렬된다. */
    private suspend fun classifyAll(rows: List<ParsedProductRow>): List<ProductCategory?> =
        coroutineScope {
            val semaphore = Semaphore(properties.aiConcurrency)
            rows
                .map { row ->
                    async { row.name?.let { name -> semaphore.withPermit { productCategoryClassifier.classify(name) } } }
                }.awaitAll()
        }

    private suspend fun matchAll(
        rows: List<ParsedProductRow>,
        categories: List<ProductCategory?>,
    ): List<String?> =
        coroutineScope {
            val semaphore = Semaphore(properties.aiConcurrency)
            rows.indices
                .map { i ->
                    async {
                        rows[i].name?.let { name -> semaphore.withPermit { productImageMatcher.match(name, categories[i]) } }
                    }
                }.awaitAll()
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

    private fun computeProgress(
        currentStep: ImportStep,
        itemRatio: Double,
    ): Int {
        val steps = ImportStep.entries
        val currentIndex = steps.indexOf(currentStep)
        val completedWeight = steps.take(currentIndex).sumOf { STEP_WEIGHTS.getValue(it) }
        val currentWeight = STEP_WEIGHTS.getValue(currentStep)
        return completedWeight + (currentWeight * itemRatio).toInt()
    }

    companion object {
        /** 계획서 §3-6. 항목 수에 비례하는 두 AI 단계에 무게를 싣는다. */
        private val STEP_WEIGHTS =
            mapOf(
                ImportStep.FILE_UPLOAD to 5,
                ImportStep.NAME_EXTRACTION to 10,
                ImportStep.PRICE_EXTRACTION to 5,
                ImportStep.CATEGORY_CLASSIFICATION to 40,
                ImportStep.IMAGE_MATCHING to 40,
            )
    }
}
