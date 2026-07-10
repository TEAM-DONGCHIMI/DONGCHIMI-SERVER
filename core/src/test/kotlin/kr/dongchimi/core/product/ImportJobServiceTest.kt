package kr.dongchimi.core.product

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.toList
import kr.dongchimi.core.common.exception.CoreException
import kr.dongchimi.core.market.Market
import kr.dongchimi.core.market.MarketErrorCode
import kr.dongchimi.core.market.MarketRepository
import kr.dongchimi.core.market.MarketValidator
import kr.dongchimi.core.upload.PresignedUpload
import kr.dongchimi.core.upload.StorageClient
import kr.dongchimi.core.upload.StoredObjectMetadata

class ImportJobServiceTest :
    FunSpec({
        val ownerId = 1L
        val marketId = 10L

        fun newJob(
            status: ImportJobStatus,
            totalCount: Int? = null,
            successCount: Int? = null,
            failCount: Int? = null,
            errorCode: String? = null,
        ) = ImportJob(
            jobId = "imp_test",
            marketId = marketId,
            ownerId = ownerId,
            excelObjectKey = "products/imports/test.xlsx",
            status = status,
            totalCount = totalCount,
            successCount = successCount,
            failCount = failCount,
            errorCode = errorCode,
        )

        fun newService(
            markets: ImportJobFakeMarketRepository = ImportJobFakeMarketRepository().apply { put(marketId, ownerId) },
            jobs: ServiceFakeImportJobRepository = ServiceFakeImportJobRepository(),
            eventChannel: ServiceFakeImportJobEventChannel = ServiceFakeImportJobEventChannel(),
            progressStore: ServiceFakeImportJobProgressStore = ServiceFakeImportJobProgressStore(),
            cancelSignal: ServiceFakeImportJobCancelSignal = ServiceFakeImportJobCancelSignal(),
            storageClient: StorageClient = ServiceFakeStorageClient(),
        ) = ImportJobService(
            marketValidator = MarketValidator(markets),
            importJobReader = ImportJobReader(jobs),
            importJobAppender = ImportJobAppender(jobs),
            importJobFinisher = ImportJobFinisher(ServiceFakePreparedProductRepository(), jobs),
            importJobCancelSignal = cancelSignal,
            importJobEventChannel = eventChannel,
            importJobProgressStore = progressStore,
            storageClient = storageClient,
        )

        test("등록 시작: 다른 점주 소유 마트면 MARKET_ACCESS_DENIED") {
            val markets = ImportJobFakeMarketRepository().apply { put(marketId, ownerId = 2L) }
            val service = newService(markets = markets)

            val exception = shouldThrow<CoreException> { service.startImport(ownerId, marketId, "https://cdn.example.com/x.xlsx") }

            exception.errorCode shouldBe MarketErrorCode.MARKET_ACCESS_DENIED
        }

        test("등록 시작: 우리 CDN 소속이 아닌 URL이면 INVALID_EXCEL_URL") {
            val service = newService(storageClient = ServiceFakeStorageClient(objectKey = null))

            val exception = shouldThrow<CoreException> { service.startImport(ownerId, marketId, "https://evil.example.com/x.xlsx") }

            exception.errorCode shouldBe ImportJobErrorCode.INVALID_EXCEL_URL
        }

        test("진행상태 구독: 존재하지 않는 jobId면 JOB_NOT_FOUND") {
            val service = newService()

            val exception = shouldThrow<CoreException> { service.subscribeProgress(ownerId, marketId, "imp_unknown").toList() }

            exception.errorCode shouldBe ImportJobErrorCode.JOB_NOT_FOUND
        }

        test("진행상태 구독: 다른 마트의 jobId면 JOB_NOT_FOUND") {
            val markets =
                ImportJobFakeMarketRepository().apply {
                    put(marketId, ownerId)
                    put(999L, ownerId)
                }
            val jobs = ServiceFakeImportJobRepository(listOf(newJob(ImportJobStatus.PENDING)))
            val service = newService(markets = markets, jobs = jobs)

            val exception = shouldThrow<CoreException> { service.subscribeProgress(ownerId, marketId = 999L, "imp_test").toList() }

            exception.errorCode shouldBe ImportJobErrorCode.JOB_NOT_FOUND
        }

        test("진행상태 구독: 이미 COMPLETED인 job은 completed 이벤트 1개만 내보내고 Redis를 조회하지 않는다") {
            val job = newJob(ImportJobStatus.COMPLETED, totalCount = 10, successCount = 8, failCount = 2)
            val jobs = ServiceFakeImportJobRepository(listOf(job))
            val eventChannel = ServiceFakeImportJobEventChannel()
            val service = newService(jobs = jobs, eventChannel = eventChannel)

            val events = service.subscribeProgress(ownerId, marketId, "imp_test").toList()

            events shouldBe listOf(ImportJobEvent.Completed(jobId = "imp_test", totalCount = 10, successCount = 8, failCount = 2))
            eventChannel.subscribeCallCount shouldBe 0
        }

        test("진행상태 구독: PENDING인 job은 진행률 0%·전 단계 PENDING인 progress 이벤트를 먼저 내보낸다") {
            val jobs = ServiceFakeImportJobRepository(listOf(newJob(ImportJobStatus.PENDING)))
            val service = newService(jobs = jobs)

            val events = service.subscribeProgress(ownerId, marketId, "imp_test").toList()

            val first = events.first().shouldBeInstanceOf<ImportJobEvent.Progress>()
            first.status shouldBe ImportJobStatus.PENDING
            first.progress shouldBe 0
            first.currentStep shouldBe null
            first.steps.map { it.status }.toSet() shouldBe setOf(ImportStepStatus.PENDING)
        }

        test("취소: PENDING인 job은 즉시 CANCELED로 전이하고 canceled 이벤트를 낸다 (워커에 신호를 보내지 않는다)") {
            val jobs = ServiceFakeImportJobRepository(listOf(newJob(ImportJobStatus.PENDING)))
            val eventChannel = ServiceFakeImportJobEventChannel()
            val cancelSignal = ServiceFakeImportJobCancelSignal()
            val service = newService(jobs = jobs, eventChannel = eventChannel, cancelSignal = cancelSignal)

            service.cancel(ownerId, marketId, "imp_test")

            jobs.find("imp_test")!!.status shouldBe ImportJobStatus.CANCELED
            eventChannel.published shouldBe listOf(ImportJobEvent.Canceled("imp_test"))
            cancelSignal.requestedJobIds.shouldBeEmpty()
        }

        test("취소: IN_PROGRESS인 job은 취소 신호만 보내고 상태는 워커가 전이할 때까지 그대로다") {
            val jobs = ServiceFakeImportJobRepository(listOf(newJob(ImportJobStatus.IN_PROGRESS)))
            val eventChannel = ServiceFakeImportJobEventChannel()
            val cancelSignal = ServiceFakeImportJobCancelSignal()
            val service = newService(jobs = jobs, eventChannel = eventChannel, cancelSignal = cancelSignal)

            service.cancel(ownerId, marketId, "imp_test")

            jobs.find("imp_test")!!.status shouldBe ImportJobStatus.IN_PROGRESS
            eventChannel.published.shouldBeEmpty()
            cancelSignal.requestedJobIds shouldBe listOf("imp_test")
        }
    })

private class ImportJobFakeMarketRepository : MarketRepository {
    private val owners = mutableMapOf<Long, Long>()

    fun put(
        id: Long,
        ownerId: Long,
    ) {
        owners[id] = ownerId
    }

    override fun findById(id: Long): Market? = null

    override fun findByOwnerId(ownerId: Long): Market? = null

    override fun findNearby(
        condition: kr.dongchimi.core.market.NearbyMarketSearchCondition,
        limit: Int,
    ): List<kr.dongchimi.core.market.NearbyMarket> = emptyList()

    override fun save(market: Market): Market = market

    override fun existsByOwnerIdAndName(
        ownerId: Long,
        name: String,
    ): Boolean = false

    override fun existsByOwnerIdAndNameAndIdNot(
        ownerId: Long,
        name: String,
        id: Long,
    ): Boolean = false

    override fun existsByIdAndOwnerId(
        marketId: Long,
        ownerId: Long,
    ): Boolean = owners[marketId] == ownerId

    override fun existsById(id: Long): Boolean = owners.containsKey(id)
}

private class ServiceFakeImportJobRepository(
    initial: List<ImportJob> = emptyList(),
) : ImportJobRepository {
    private val store = initial.associateBy { it.jobId }.toMutableMap()

    override fun append(job: ImportJob): ImportJob {
        store[job.jobId] = job
        return job
    }

    override fun find(jobId: String): ImportJob? = store[jobId]

    override fun claimNext(
        instanceId: String,
        lease: java.time.Duration,
        maxAttempts: Int,
    ): ImportJob? = null

    override fun renewLease(
        jobId: String,
        instanceId: String,
        lease: java.time.Duration,
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

private class ServiceFakeImportJobEventChannel : ImportJobEventChannel {
    val published = mutableListOf<ImportJobEvent>()
    var subscribeCallCount = 0
        private set

    override suspend fun publish(event: ImportJobEvent) {
        published += event
    }

    override fun subscribe(jobId: String): Flow<ImportJobEvent> {
        subscribeCallCount++
        return emptyFlow()
    }
}

private class ServiceFakeImportJobProgressStore : ImportJobProgressStore {
    private val store = mutableMapOf<String, ImportJobProgress>()

    override suspend fun save(progress: ImportJobProgress) {
        store[progress.jobId] = progress
    }

    override suspend fun find(jobId: String): ImportJobProgress? = store[jobId]
}

private class ServiceFakeImportJobCancelSignal : ImportJobCancelSignal {
    val requestedJobIds = mutableListOf<String>()

    override suspend fun request(jobId: String) {
        requestedJobIds += jobId
    }

    override suspend fun isRequested(jobId: String): Boolean = jobId in requestedJobIds

    override fun subscribeControl(jobId: String): Flow<Unit> = emptyFlow()
}

private class ServiceFakeStorageClient(
    private val objectKey: String? = "products/imports/test.xlsx",
) : StorageClient {
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

    override fun resolveObjectKey(accessUrl: String): String? = objectKey

    override fun download(objectKey: String): ByteArray = ByteArray(0)
}

private class ServiceFakePreparedProductRepository : PreparedProductRepository {
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

    override fun saveAll(preparedProducts: List<PreparedProduct>): List<PreparedProduct> = preparedProducts

    override fun softDeleteAllByMarketId(marketId: Long) {}
}
