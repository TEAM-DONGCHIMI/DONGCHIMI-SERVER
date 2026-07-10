package kr.dongchimi.db.product

import io.kotest.core.extensions.ApplyExtension
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kr.dongchimi.core.product.ImportJob
import kr.dongchimi.core.product.ImportJobRepository
import kr.dongchimi.core.product.ImportJobResult
import kr.dongchimi.core.product.ImportJobStatus
import kr.dongchimi.db.testsupport.TestJpaConfig
import kr.dongchimi.db.testsupport.TestPostgresContainer
import org.springframework.test.context.ContextConfiguration
import java.time.Duration
import java.util.Collections
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@ContextConfiguration(classes = [TestJpaConfig::class])
@ApplyExtension(SpringExtension::class)
class ImportJobRepositoryImplTest(
    repository: ImportJobRepository,
) : FunSpec({
        beforeEach {
            TestPostgresContainer.newConnection().use { connection ->
                connection.createStatement().use { it.execute("TRUNCATE TABLE product_import_jobs") }
            }
        }

        fun newJob() =
            ImportJob(
                jobId = "imp_${UUID.randomUUID()}",
                marketId = 1L,
                ownerId = 1L,
                excelObjectKey = "tmp/import/test.xlsx",
                status = ImportJobStatus.PENDING,
            )

        test("PENDING 작업을 claim하면 IN_PROGRESS로 바뀌고 attempt_count가 증가한다") {
            val job = repository.append(newJob())

            val claimed = repository.claimNext("instance-a", Duration.ofMinutes(1), maxAttempts = 5)

            claimed.shouldNotBeNull()
            claimed.jobId shouldBe job.jobId
            claimed.status shouldBe ImportJobStatus.IN_PROGRESS
            claimed.attemptCount shouldBe 1
        }

        test("claim할 작업이 없으면 null을 반환한다") {
            repository.claimNext("instance-a", Duration.ofMinutes(1), maxAttempts = 5).shouldBeNull()
        }

        test("리스가 만료된 IN_PROGRESS 작업을 회수하고 attempt_count가 다시 증가한다") {
            val job = repository.append(newJob())
            // 음수 리스를 줘서 즉시 만료된 상태로 만든다(스레드 슬립 없이 결정적으로 재현)
            repository.claimNext("instance-a", Duration.ofSeconds(-1), maxAttempts = 5)

            val recovered = repository.claimNext("instance-b", Duration.ofMinutes(1), maxAttempts = 5)

            recovered.shouldNotBeNull()
            recovered.jobId shouldBe job.jobId
            recovered.attemptCount shouldBe 2
        }

        test("attempt_count가 상한이면 claim하지 않는다") {
            repository.append(newJob())
            repeat(3) {
                repository.claimNext("instance-a", Duration.ofSeconds(-1), maxAttempts = 3)
            }

            repository.claimNext("instance-b", Duration.ofMinutes(1), maxAttempts = 3).shouldBeNull()
        }

        test("releaseLeases는 해당 인스턴스가 잡은 작업만 PENDING으로 되돌린다") {
            val jobA = repository.append(newJob())
            val jobB = repository.append(newJob())
            repository.claimNext("instance-a", Duration.ofMinutes(1), maxAttempts = 5)
            repository.claimNext("instance-b", Duration.ofMinutes(1), maxAttempts = 5)

            repository.releaseLeases("instance-a")

            val releasedJobA = repository.find(jobA.jobId)!!
            releasedJobA.status shouldBe ImportJobStatus.PENDING
            releasedJobA.attemptCount shouldBe 0
            repository.find(jobB.jobId)!!.status shouldBe ImportJobStatus.IN_PROGRESS
        }

        test("compareAndFinish는 이미 종료된 작업에 대해 false를 반환하고 값을 바꾸지 않는다") {
            val job = repository.append(newJob())
            repository.claimNext("instance-a", Duration.ofMinutes(1), maxAttempts = 5)
            repository.compareAndFinish(
                job.jobId,
                ImportJobStatus.COMPLETED,
                ImportJobResult(totalCount = 10, successCount = 10, failCount = 0),
            )

            val result =
                repository.compareAndFinish(
                    job.jobId,
                    ImportJobStatus.FAILED,
                    ImportJobResult(errorCode = "ANALYSIS_FAILED"),
                )

            result.shouldBeFalse()
        }

        test("compareAndFinish는 IN_PROGRESS 작업을 종료 상태로 전이시킨다") {
            val job = repository.append(newJob())
            repository.claimNext("instance-a", Duration.ofMinutes(1), maxAttempts = 5)

            val result =
                repository.compareAndFinish(
                    job.jobId,
                    ImportJobStatus.COMPLETED,
                    ImportJobResult(totalCount = 5, successCount = 4, failCount = 1),
                )

            result.shouldBeTrue()
            repository.find(job.jobId)!!.status shouldBe ImportJobStatus.COMPLETED
        }

        test("두 스레드가 동시에 claim해도 같은 job을 두 번 집지 않는다") {
            val job = repository.append(newJob())
            val executor = Executors.newFixedThreadPool(2)
            val startLatch = CountDownLatch(1)
            val results = Collections.synchronizedList(mutableListOf<ImportJob?>())

            val futures =
                (1..2).map { i ->
                    executor.submit {
                        startLatch.await()
                        results.add(repository.claimNext("instance-$i", Duration.ofMinutes(1), maxAttempts = 5))
                    }
                }
            startLatch.countDown()
            futures.forEach { it.get(10, TimeUnit.SECONDS) }
            executor.shutdown()

            val claimed = results.filterNotNull()
            claimed shouldHaveSize 1
            claimed.first().jobId shouldBe job.jobId
        }
    })
