package kr.dongchimi.core.viewcount

import io.github.oshai.kotlinlogging.KotlinLogging
import kr.dongchimi.core.market.MarketMetadataRepository
import kr.dongchimi.core.product.ProductMetadataRepository
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

/**
 * Redis 누적분을 걷어내 metadata 테이블에 일괄 반영한다. 상품·마트 조회수가 흐름이 동일해
 * 한 곳에서 타깃별로 디스패치하는 교차 도메인 쓰기 지점이다.
 */
@Component
class ViewCountFlusher(
    private val viewCountStore: ViewCountStore,
    private val productMetadataRepository: ProductMetadataRepository,
    private val marketMetadataRepository: MarketMetadataRepository,
) {
    fun flush() {
        // 타깃별로 독립 처리한다. DB 반영에 성공한 배치만 commit해 저장소에서 비우고,
        // 실패하면 restore로 되돌려 다음 주기에 재처리한다(반영 전 삭제로 인한 유실 방지).
        ViewTarget.entries.forEach { target ->
            val batch = viewCountStore.drain(target) ?: return@forEach

            try {
                when (target) {
                    ViewTarget.PRODUCT -> productMetadataRepository.incrementViewCounts(batch.deltas)
                    ViewTarget.MARKET -> marketMetadataRepository.incrementViewCounts(batch.deltas)
                }
                viewCountStore.commit(batch)
            } catch (e: Exception) {
                logger.warn(e) { "조회수 flush 실패 — 되돌려 다음 주기에 재처리 target=$target" }
                viewCountStore.restore(batch)
            }
        }
    }
}
