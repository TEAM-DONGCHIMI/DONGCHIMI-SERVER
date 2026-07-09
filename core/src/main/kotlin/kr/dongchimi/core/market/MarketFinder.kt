package kr.dongchimi.core.market

import kr.dongchimi.core.common.CursorSliceResult
import org.springframework.stereotype.Component

@Component
class MarketFinder(
    private val marketRepository: MarketRepository,
) {
    fun findNearby(condition: NearbyMarketSearchCondition): CursorSliceResult<NearbyMarket> {
        // 다음 페이지 존재 여부를 알기 위해 한 건 더 조회한다.
        val markets = marketRepository.findNearby(condition, condition.size + 1)
        val content = markets.take(condition.size)
        val hasNext = markets.size > condition.size

        return CursorSliceResult(
            content = content,
            hasNext = hasNext,
            nextCursor = if (hasNext) content.last().market.id else null,
        )
    }
}
