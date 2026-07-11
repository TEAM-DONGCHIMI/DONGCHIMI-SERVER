package kr.dongchimi.core.viewcount

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

/** 주기적으로 Redis 누적분을 DB로 flush한다. 모든 인스턴스가 돌아도 drain이 원자적이라 중복 반영되지 않는다. */
@Component
class ViewCountSyncScheduler(
    private val viewCountFlusher: ViewCountFlusher,
) {
    @Scheduled(fixedDelayString = "\${view-count.flush-interval}")
    fun flush() {
        viewCountFlusher.flush()
    }
}
