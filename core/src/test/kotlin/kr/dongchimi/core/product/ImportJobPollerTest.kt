package kr.dongchimi.core.product

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kr.dongchimi.core.product.import.ImportJob
import kr.dongchimi.core.product.import.ImportJobPoller
import kr.dongchimi.core.product.import.ImportJobProperties
import kr.dongchimi.core.product.import.ImportJobRepository
import kr.dongchimi.core.product.import.ImportJobResult
import kr.dongchimi.core.product.import.ImportJobRunner
import kr.dongchimi.core.product.import.ImportJobStatus
import java.time.Duration

class ImportJobPollerTest :
    FunSpec({
        fun testProperties(slots: Int = 4) =
            ImportJobProperties(
                lease = Duration.ofMinutes(1),
                // 이 테스트는 pollOnce()를 직접 호출한다 — 백그라운드 루프(start())는 쓰지 않는다.
                pollInterval = Duration.ofMinutes(10),
                slotsPerInstance = slots,
                maxAttempts = 5,
                aiConcurrency = 4,
            )

        fun newJob(jobId: String = "imp_1") =
            ImportJob(
                jobId = jobId,
                marketId = 1L,
                ownerId = 1L,
                excelObjectKey = "k",
                status = ImportJobStatus.PENDING,
            )

        test("pollOnce는 claim에 성공하면 러너에 위임한다") {
            val job = newJob()
            val repository = FakePollerImportJobRepository(claimResults = mutableListOf(job))
            val runner = FakeImportJobRunner()
            val poller = ImportJobPoller(repository, runner, testProperties())

            poller.pollOnce()
            delay(200) // pollOnce는 처리를 별도 코루틴으로 넘기고 바로 리턴하므로 완료를 잠깐 기다린다

            runner.ranJobs shouldContainExactly listOf(job)
        }

        test("claim할 job이 없으면 러너를 부르지 않는다") {
            val repository = FakePollerImportJobRepository(claimResults = mutableListOf(null))
            val runner = FakeImportJobRunner()
            val poller = ImportJobPoller(repository, runner, testProperties())

            poller.pollOnce()
            delay(200)

            runner.ranJobs.shouldBeEmpty()
        }

        test("shutdown 시 이 인스턴스가 잡은 job의 리스를 반납한다") {
            val repository = FakePollerImportJobRepository(claimResults = mutableListOf(null))
            val poller = ImportJobPoller(repository, FakeImportJobRunner(), testProperties())

            poller.shutdown()

            repository.releasedInstanceIds shouldContainExactly listOf(poller.instanceId)
        }

        test("슬롯 수만큼만 동시에 claim한다 — 슬롯이 다 차면 처리가 끝나야 다음 job을 집는다") {
            val jobs = listOf(newJob("imp_1"), newJob("imp_2"))
            val repository = FakePollerImportJobRepository(claimResults = jobs.toMutableList())
            val runner = FakeImportJobRunner(holdUntilReleased = true)
            val poller = ImportJobPoller(repository, runner, testProperties(slots = 1))

            poller.pollOnce() // 슬롯 1개를 다 씀
            delay(100)
            runner.ranJobs shouldContainExactly listOf(jobs[0])

            runner.release() // 첫 job "완료" 시뮬레이션 → 슬롯 반납
            delay(100)

            poller.pollOnce()
            delay(100)
            runner.ranJobs shouldContainExactly jobs
        }
    })

private class FakePollerImportJobRepository(
    private val claimResults: MutableList<ImportJob?>,
) : ImportJobRepository {
    val releasedInstanceIds = mutableListOf<String>()

    override fun append(job: ImportJob): ImportJob = job

    override fun find(jobId: String): ImportJob? = null

    override fun claimNext(
        instanceId: String,
        lease: Duration,
        maxAttempts: Int,
    ): ImportJob? = if (claimResults.isNotEmpty()) claimResults.removeAt(0) else null

    override fun renewLease(
        jobId: String,
        instanceId: String,
        lease: Duration,
    ): Boolean = true

    override fun releaseLeases(instanceId: String) {
        releasedInstanceIds += instanceId
    }

    override fun compareAndFinish(
        jobId: String,
        status: ImportJobStatus,
        result: ImportJobResult?,
    ): Boolean = true
}

private class FakeImportJobRunner(
    private val holdUntilReleased: Boolean = false,
) : ImportJobRunner {
    val ranJobs = mutableListOf<ImportJob>()
    private val gate = Channel<Unit>(Channel.UNLIMITED)

    override suspend fun run(
        job: ImportJob,
        instanceId: String,
    ) {
        ranJobs += job
        if (holdUntilReleased) gate.receive()
    }

    fun release() {
        gate.trySend(Unit)
    }
}
