package kr.dongchimi.core.viewcount

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

private val logger = KotlinLogging.logger {}

/**
 * 상세 조회 트랜잭션이 커밋된 뒤에만 조회수를 반영한다(롤백·미존재 조회는 집계 제외).
 * Redis 장애가 조회 응답에 영향을 주지 않도록 예외를 삼키고 로깅만 한다.
 */
@Component
class ViewCountEventListener(
    private val viewCountStore: ViewCountStore,
) {
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handle(event: EntityViewedEvent) {
        try {
            viewCountStore.record(event.target, event.targetId, event.userId)
        } catch (e: Exception) {
            logger.warn(e) { "조회수 기록 실패 — target=${event.target} id=${event.targetId}" }
        }
    }
}
