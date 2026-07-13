// [임시] 엑셀 분석 파이프라인 동기/비동기 성능 비교용. 벤치마크 종료 후 제거.
package kr.dongchimi.core.product.importjob.sync

import kotlinx.coroutines.runBlocking
import kr.dongchimi.core.common.exception.CoreException
import kr.dongchimi.core.market.MarketValidator
import kr.dongchimi.core.product.DraftFailReasonResolver
import kr.dongchimi.core.product.DraftStatus
import kr.dongchimi.core.product.PreparedProduct
import kr.dongchimi.core.product.ProductCategory
import kr.dongchimi.core.product.importjob.ExcelProductParser
import kr.dongchimi.core.product.importjob.ImportJobErrorCode
import kr.dongchimi.core.product.importjob.ImportJobProperties
import kr.dongchimi.core.product.importjob.ParsedProductRow
import kr.dongchimi.core.product.importjob.ProductCategoryClassifier
import kr.dongchimi.core.product.importjob.ProductCategoryClassifyItem
import kr.dongchimi.core.product.importjob.ProductImageMatchItem
import kr.dongchimi.core.product.importjob.ProductImageMatcher
import kr.dongchimi.core.upload.StorageClient
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

/**
 * 큐·워커·SSE 없이 요청 스레드에서 파이프라인을 처음부터 끝까지 블로킹 실행한다. AI 두 단계는
 * 비동기 버전([kr.dongchimi.core.product.importjob.worker.ImportJobProcessor])과 동일하게 배치(청크)로
 * 묶되, `async`/`Semaphore`를 제거해 청크를 **순차** 호출한다 — 동시성 이득만 격리해 측정하기 위함이다.
 */
@Service
class SyncImportJobService(
    private val marketValidator: MarketValidator,
    private val storageClient: StorageClient,
    private val excelProductParser: ExcelProductParser,
    private val productCategoryClassifier: ProductCategoryClassifier,
    private val productImageMatcher: ProductImageMatcher,
    private val draftFailReasonResolver: DraftFailReasonResolver,
    private val syncImportPersister: SyncImportPersister,
    private val properties: ImportJobProperties,
) {
    fun analyze(
        ownerId: Long,
        marketId: Long,
        excelFileUrl: String,
    ): SyncImportResult {
        marketValidator.validateOwnership(marketId, ownerId)
        val objectKey =
            storageClient.resolveObjectKey(excelFileUrl) ?: throw CoreException(ImportJobErrorCode.INVALID_EXCEL_URL)

        val totalStart = System.nanoTime()

        val (bytes, downloadNs) = measured { storageClient.download(objectKey) }
        val (rows, parseNs) = measured { excelProductParser.parse(bytes) }
        val (categories, classifyNs) = measured { classifySequentially(rows) }
        val (thumbnails, matchNs) = measured { matchSequentially(rows, categories) }

        val drafts = buildDrafts(marketId, rows, categories, thumbnails)
        val persistNs = measured { syncImportPersister.persist(marketId, drafts) }.second

        val elapsedNs = System.nanoTime() - totalStart

        return SyncImportResult(
            totalCount = drafts.size,
            successCount = drafts.count { it.draftStatus == DraftStatus.SUCCESS },
            failCount = drafts.count { it.draftStatus == DraftStatus.FAIL },
            elapsedMs = elapsedNs.toMillis(),
            stageElapsedMs =
                StageElapsedMs(
                    download = downloadNs.toMillis(),
                    parse = parseNs.toMillis(),
                    classify = classifyNs.toMillis(),
                    match = matchNs.toMillis(),
                    persist = persistNs.toMillis(),
                ),
        )
    }

    /** 청크로 묶어 순차 호출한다(동시성 없음). 결과는 rows와 같은 순서로 정렬한다. */
    private fun classifySequentially(rows: List<ParsedProductRow>): List<ProductCategory?> {
        val items =
            rows.withIndex().mapNotNull { (index, row) ->
                row.name?.let { ProductCategoryClassifyItem(index, it) }
            }

        val results = mutableMapOf<Int, ProductCategory?>()
        runBlocking {
            items.chunked(properties.aiBatchSize).forEach { chunk ->
                results += productCategoryClassifier.classify(chunk)
            }
        }
        return rows.indices.map { results[it] }
    }

    /** 분류 성공(category != null) 행만 청크로 묶어 순차 호출한다. */
    private fun matchSequentially(
        rows: List<ParsedProductRow>,
        categories: List<ProductCategory?>,
    ): List<String?> {
        val items =
            rows.indices.mapNotNull { i ->
                val name = rows[i].name ?: return@mapNotNull null
                val category = categories[i] ?: return@mapNotNull null // 분류 실패 행은 매칭을 시도하지 않는다
                ProductImageMatchItem(i, name, category)
            }

        val results = mutableMapOf<Int, String?>()
        runBlocking {
            items.chunked(properties.aiBatchSize).forEach { chunk ->
                results += productImageMatcher.match(chunk)
            }
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

    private inline fun <T> measured(block: () -> T): Pair<T, Long> {
        val start = System.nanoTime()
        val result = block()
        return result to (System.nanoTime() - start)
    }

    private fun Long.toMillis(): Long = TimeUnit.NANOSECONDS.toMillis(this)
}
