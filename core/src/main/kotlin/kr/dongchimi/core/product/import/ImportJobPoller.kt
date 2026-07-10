package kr.dongchimi.core.product.import

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.slf4j.MDCContext
import kotlinx.coroutines.sync.Semaphore
import org.springframework.stereotype.Component
import java.util.UUID

private val logger = KotlinLogging.logger {}

/**
 * 인스턴스별로 하나씩 뜨는 큐 폴러. start()는 @PostConstruct로만 걸어서, 테스트에서
 * 인스턴스를 직접 생성해도(스프링 컨텍스트 없이) 백그라운드 루프가 멋대로 돌지 않게 한다
 * — pollOnce()를 직접 호출해서 검증한다.
 */
@Component
class ImportJobPoller(
    private val importJobRepository: ImportJobRepository,
    private val importJobRunner: ImportJobRunner,
    private val properties: ImportJobProperties,
) {
    val instanceId: String = UUID.randomUUID().toString()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val semaphore = Semaphore(properties.slotsPerInstance)

    @PostConstruct
    fun start() {
        scope.launch(MDCContext()) {
            while (isActive) {
                delay(properties.pollInterval.toMillis())
                pollOnce()
            }
        }
    }

    /** 슬롯이 있으면 claim을 시도하고, 잡히면 별도 코루틴으로 처리를 넘긴 뒤 즉시 다음 폴링으로 돌아간다. */
    suspend fun pollOnce() {
        semaphore.acquire()

        val job = importJobRepository.claimNext(instanceId, properties.lease, properties.maxAttempts)
        if (job == null) {
            semaphore.release()
            return
        }

        scope.launch(MDCContext()) {
            try {
                importJobRunner.run(job, instanceId)
            } catch (e: Exception) {
                logger.error(e) { "워커 실행 중 처리되지 않은 예외: jobId=${job.jobId}" }
            } finally {
                semaphore.release()
            }
        }
    }

    /** 이 인스턴스가 잡고 있던 job을 PENDING으로 되돌린다 — green이 리스 만료를 기다리지 않고 즉시 이어받는다. */
    @PreDestroy
    fun shutdown() {
        scope.cancel()
        importJobRepository.releaseLeases(instanceId)
    }
}
