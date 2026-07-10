package kr.dongchimi.core.product

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kr.dongchimi.core.upload.PresignedUpload
import kr.dongchimi.core.upload.StorageClient
import kr.dongchimi.core.upload.StoredObjectMetadata
import java.math.BigDecimal
import java.time.Duration
import java.time.LocalDate

class ImportJobProcessorTest :
    FunSpec({
        fun testProperties() =
            ImportJobProperties(
                lease = Duration.ofMinutes(1),
                pollInterval = Duration.ofSeconds(1),
                slotsPerInstance = 4,
                maxAttempts = 5,
                aiConcurrency = 4,
            )

        fun testRow(name: String = "콩나물") =
            ParsedProductRow(
                name = name,
                price = Price(BigDecimal(1000), BigDecimal(1000)),
                discountPeriod = DiscountPeriod(LocalDate.now(), LocalDate.now().plusDays(3)),
                promotionalPhrase = null,
            )

        fun newJob(marketId: Long = 1L) =
            ImportJob(
                jobId = "imp_test",
                marketId = marketId,
                ownerId = 1L,
                excelObjectKey = "products/imports/test.xlsx",
                status = ImportJobStatus.IN_PROGRESS,
            )

        test("5단계가 순서대로 progress 이벤트를 emit하고 마지막에 completed") {
            val job = newJob()
            val importJobRepository = FakeImportJobRepository(listOf(job))
            val preparedProductRepository = ImportJobFakePreparedProductRepository()
            val eventChannel = FakeImportJobEventChannel()
            val parser = FakeExcelProductParser(listOf(testRow()))
            val processor =
                ImportJobProcessor(
                    importJobRepository = importJobRepository,
                    storageClient = FakeStorageClient(),
                    excelProductParser = parser,
                    productCategoryClassifier = FakeProductCategoryClassifier(ProductCategory.VEGETABLE_FRUIT),
                    productImageMatcher = FakeProductImageMatcher("https://cdn.example.com/x.jpg"),
                    importJobProgressStore = FakeImportJobProgressStore(),
                    importJobEventChannel = eventChannel,
                    importJobCancelSignal = FakeImportJobCancelSignal(),
                    importJobFinisher = ImportJobFinisher(preparedProductRepository, importJobRepository),
                    draftFailReasonResolver = DraftFailReasonResolver(),
                    properties = testProperties(),
                )

            processor.run(job, "instance-a")

            val progressSteps = eventChannel.published.filterIsInstance<ImportJobEvent.Progress>().map { it.currentStep }
            progressSteps shouldContainExactly
                listOf(
                    ImportStep.FILE_UPLOAD,
                    ImportStep.FILE_UPLOAD,
                    ImportStep.NAME_EXTRACTION,
                    ImportStep.NAME_EXTRACTION,
                    ImportStep.PRICE_EXTRACTION,
                    ImportStep.PRICE_EXTRACTION,
                    ImportStep.CATEGORY_CLASSIFICATION,
                    ImportStep.CATEGORY_CLASSIFICATION,
                    ImportStep.IMAGE_MATCHING,
                    ImportStep.IMAGE_MATCHING,
                )
            eventChannel.published.last().shouldBeInstanceOf<ImportJobEvent.Completed>()
        }

        test("완료 시 기존 draft를 soft delete하고 분석 결과를 새 draft로 삽입한다") {
            val job = newJob(marketId = 42L)
            val importJobRepository = FakeImportJobRepository(listOf(job))
            val preparedProductRepository = ImportJobFakePreparedProductRepository()
            val processor =
                ImportJobProcessor(
                    importJobRepository = importJobRepository,
                    storageClient = FakeStorageClient(),
                    excelProductParser = FakeExcelProductParser(listOf(testRow(), testRow("두부"))),
                    productCategoryClassifier = FakeProductCategoryClassifier(ProductCategory.VEGETABLE_FRUIT),
                    productImageMatcher = FakeProductImageMatcher("https://cdn.example.com/x.jpg"),
                    importJobProgressStore = FakeImportJobProgressStore(),
                    importJobEventChannel = FakeImportJobEventChannel(),
                    importJobCancelSignal = FakeImportJobCancelSignal(),
                    importJobFinisher = ImportJobFinisher(preparedProductRepository, importJobRepository),
                    draftFailReasonResolver = DraftFailReasonResolver(),
                    properties = testProperties(),
                )

            processor.run(job, "instance-a")

            preparedProductRepository.softDeletedMarketIds shouldContainExactly listOf(42L)
            preparedProductRepository.savedBatches.single().size shouldBe 2
        }

        test("파싱은 1회만 수행되고 NAME_EXTRACTION/PRICE_EXTRACTION 이벤트는 2회씩 나간다") {
            val job = newJob()
            val importJobRepository = FakeImportJobRepository(listOf(job))
            val parser = FakeExcelProductParser(listOf(testRow()))
            val processor =
                ImportJobProcessor(
                    importJobRepository = importJobRepository,
                    storageClient = FakeStorageClient(),
                    excelProductParser = parser,
                    productCategoryClassifier = FakeProductCategoryClassifier(ProductCategory.VEGETABLE_FRUIT),
                    productImageMatcher = FakeProductImageMatcher("https://cdn.example.com/x.jpg"),
                    importJobProgressStore = FakeImportJobProgressStore(),
                    importJobEventChannel = FakeImportJobEventChannel(),
                    importJobCancelSignal = FakeImportJobCancelSignal(),
                    importJobFinisher = ImportJobFinisher(ImportJobFakePreparedProductRepository(), importJobRepository),
                    draftFailReasonResolver = DraftFailReasonResolver(),
                    properties = testProperties(),
                )

            processor.run(job, "instance-a")

            parser.parseCallCount shouldBe 1
        }

        test("중간에 취소 요청이 들어오면 canceled 이벤트를 내고 prepared_products는 건드리지 않는다") {
            val job = newJob()
            val importJobRepository = FakeImportJobRepository(listOf(job))
            val preparedProductRepository = ImportJobFakePreparedProductRepository()
            val eventChannel = FakeImportJobEventChannel()
            val cancelSignal = FakeImportJobCancelSignal()
            // NAME_EXTRACTION(파싱) 도중 취소가 접수됐다고 가정한다 — 다음 체크포인트(PRICE_EXTRACTION)에서 잡힌다.
            val parser = FakeExcelProductParser(listOf(testRow())) { cancelSignal.requested = true }
            val processor =
                ImportJobProcessor(
                    importJobRepository = importJobRepository,
                    storageClient = FakeStorageClient(),
                    excelProductParser = parser,
                    productCategoryClassifier = FakeProductCategoryClassifier(ProductCategory.VEGETABLE_FRUIT),
                    productImageMatcher = FakeProductImageMatcher("https://cdn.example.com/x.jpg"),
                    importJobProgressStore = FakeImportJobProgressStore(),
                    importJobEventChannel = eventChannel,
                    importJobCancelSignal = cancelSignal,
                    importJobFinisher = ImportJobFinisher(preparedProductRepository, importJobRepository),
                    draftFailReasonResolver = DraftFailReasonResolver(),
                    properties = testProperties(),
                )

            processor.run(job, "instance-a")

            eventChannel.published.last().shouldBeInstanceOf<ImportJobEvent.Canceled>()
            preparedProductRepository.savedBatches.shouldBeEmpty()
            preparedProductRepository.softDeletedMarketIds.shouldBeEmpty()
            importJobRepository.find(job.jobId)!!.status shouldBe ImportJobStatus.CANCELED
        }

        test("이미 완료 커밋된 job은 이후 취소 요청이 들어와도 completed 상태를 유지한다 (CAS)") {
            val job = newJob()
            val importJobRepository = FakeImportJobRepository(listOf(job))
            val preparedProductRepository = ImportJobFakePreparedProductRepository()
            val finisher = ImportJobFinisher(preparedProductRepository, importJobRepository)
            val processor =
                ImportJobProcessor(
                    importJobRepository = importJobRepository,
                    storageClient = FakeStorageClient(),
                    excelProductParser = FakeExcelProductParser(listOf(testRow())),
                    productCategoryClassifier = FakeProductCategoryClassifier(ProductCategory.VEGETABLE_FRUIT),
                    productImageMatcher = FakeProductImageMatcher("https://cdn.example.com/x.jpg"),
                    importJobProgressStore = FakeImportJobProgressStore(),
                    importJobEventChannel = FakeImportJobEventChannel(),
                    importJobCancelSignal = FakeImportJobCancelSignal(),
                    importJobFinisher = finisher,
                    draftFailReasonResolver = DraftFailReasonResolver(),
                    properties = testProperties(),
                )
            processor.run(job, "instance-a")

            val canceledAfterCompletion = finisher.cancel(job.jobId)

            canceledAfterCompletion shouldBe false
            importJobRepository.find(job.jobId)!!.status shouldBe ImportJobStatus.COMPLETED
        }

        test("DraftFailReasonResolver가 failReason을 결정한다 (파서·매처가 직접 정하지 않는다)") {
            val job = newJob()
            val importJobRepository = FakeImportJobRepository(listOf(job))
            val preparedProductRepository = ImportJobFakePreparedProductRepository()
            val processor =
                ImportJobProcessor(
                    importJobRepository = importJobRepository,
                    storageClient = FakeStorageClient(),
                    // 카테고리 매칭 실패(null)를 흉내낸다
                    excelProductParser = FakeExcelProductParser(listOf(testRow())),
                    productCategoryClassifier = FakeProductCategoryClassifier(category = null),
                    productImageMatcher = FakeProductImageMatcher(thumbnailUrl = null),
                    importJobProgressStore = FakeImportJobProgressStore(),
                    importJobEventChannel = FakeImportJobEventChannel(),
                    importJobCancelSignal = FakeImportJobCancelSignal(),
                    importJobFinisher = ImportJobFinisher(preparedProductRepository, importJobRepository),
                    draftFailReasonResolver = DraftFailReasonResolver(),
                    properties = testProperties(),
                )

            processor.run(job, "instance-a")

            val saved = preparedProductRepository.savedBatches.single().single()
            saved.draftStatus shouldBe DraftStatus.FAIL
            saved.failReason shouldBe DraftFailReason.THUMBNAIL_MISSING
        }
    })

private class FakeStorageClient : StorageClient {
    override fun createUploadUrl(
        objectKey: String,
        contentType: String,
        contentLength: Long,
    ): PresignedUpload = throw UnsupportedOperationException()

    override fun getObjectMetadata(objectKey: String): StoredObjectMetadata? = null

    override fun moveObject(
        sourceKey: String,
        destinationKey: String,
    ) {}

    override fun resolveAccessUrl(objectKey: String): String = ""

    override fun resolveObjectKey(accessUrl: String): String? = null

    override fun download(objectKey: String): ByteArray = ByteArray(0)
}

private class FakeExcelProductParser(
    private val rows: List<ParsedProductRow>,
    private val onParse: () -> Unit = {},
) : ExcelProductParser {
    var parseCallCount = 0
        private set

    override fun parse(bytes: ByteArray): List<ParsedProductRow> {
        parseCallCount++
        onParse()
        return rows
    }
}

private class FakeProductCategoryClassifier(
    private val category: ProductCategory?,
) : ProductCategoryClassifier {
    override suspend fun classify(productName: String): ProductCategory? = category
}

private class FakeProductImageMatcher(
    private val thumbnailUrl: String?,
) : ProductImageMatcher {
    override suspend fun match(
        productName: String,
        category: ProductCategory?,
    ): String? = thumbnailUrl
}

private class FakeImportJobProgressStore : ImportJobProgressStore {
    val saved = mutableListOf<ImportJobProgress>()

    override suspend fun save(progress: ImportJobProgress) {
        saved += progress
    }

    override suspend fun find(jobId: String): ImportJobProgress? = saved.lastOrNull { it.jobId == jobId }
}

private class FakeImportJobEventChannel : ImportJobEventChannel {
    val published = mutableListOf<ImportJobEvent>()

    override suspend fun publish(event: ImportJobEvent) {
        published += event
    }

    override fun subscribe(jobId: String): Flow<ImportJobEvent> = emptyFlow()
}

private class FakeImportJobCancelSignal : ImportJobCancelSignal {
    var requested = false

    override suspend fun request(jobId: String) {
        requested = true
    }

    override suspend fun isRequested(jobId: String): Boolean = requested

    override fun subscribeControl(jobId: String): Flow<Unit> = emptyFlow()
}

private class FakeImportJobRepository(
    initial: List<ImportJob>,
) : ImportJobRepository {
    private val store = initial.associateBy { it.jobId }.toMutableMap()

    override fun append(job: ImportJob): ImportJob {
        store[job.jobId] = job
        return job
    }

    override fun find(jobId: String): ImportJob? = store[jobId]

    override fun claimNext(
        instanceId: String,
        lease: Duration,
        maxAttempts: Int,
    ): ImportJob? = null

    override fun renewLease(
        jobId: String,
        instanceId: String,
        lease: Duration,
    ): Boolean = true

    override fun releaseLeases(instanceId: String) {}

    override fun compareAndFinish(
        jobId: String,
        status: ImportJobStatus,
        result: ImportJobResult?,
    ): Boolean {
        val current = store[jobId] ?: return false
        if (current.status in TERMINAL_STATUSES) return false

        store[jobId] = current.copy(status = status)
        return true
    }

    companion object {
        private val TERMINAL_STATUSES = setOf(ImportJobStatus.COMPLETED, ImportJobStatus.FAILED, ImportJobStatus.CANCELED)
    }
}

private class ImportJobFakePreparedProductRepository : PreparedProductRepository {
    val savedBatches = mutableListOf<List<PreparedProduct>>()
    val softDeletedMarketIds = mutableListOf<Long>()

    override fun findDrafts(
        marketId: Long,
        condition: PreparedProductSearchCondition,
        pageOffset: kr.dongchimi.core.common.PageOffset,
    ): List<PreparedProduct> = emptyList()

    override fun countDrafts(marketId: Long): PreparedProductDraftCounts = PreparedProductDraftCounts(0, 0, 0)

    override fun findAllByMarketId(marketId: Long): List<PreparedProduct> = emptyList()

    override fun countInMarket(
        ids: List<Long>,
        marketId: Long,
    ): Int = 0

    override fun updateDrafts(
        commands: List<PreparedProductDraftSaveCommand>,
        failReasons: Map<Long, DraftFailReason?>,
    ) {}

    override fun softDeleteByIds(ids: List<Long>) {}

    override fun saveAll(preparedProducts: List<PreparedProduct>): List<PreparedProduct> {
        savedBatches += preparedProducts
        return preparedProducts
    }

    override fun softDeleteAllByMarketId(marketId: Long) {
        softDeletedMarketIds += marketId
    }
}
