package kr.dongchimi.core.viewcount

import kr.dongchimi.core.market.MarketMetadataRepository
import kr.dongchimi.core.product.ProductMetadataRepository
import org.springframework.stereotype.Component

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
        ViewTarget.entries.forEach { target ->
            val deltas = viewCountStore.drain(target)
            if (deltas.isEmpty()) return@forEach

            when (target) {
                ViewTarget.PRODUCT -> productMetadataRepository.incrementViewCounts(deltas)
                ViewTarget.MARKET -> marketMetadataRepository.incrementViewCounts(deltas)
            }
        }
    }
}
