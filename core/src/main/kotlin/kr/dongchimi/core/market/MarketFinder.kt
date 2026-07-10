package kr.dongchimi.core.market

import kr.dongchimi.core.common.CursorSliceResult
import kr.dongchimi.core.common.toCursorSlice
import org.springframework.stereotype.Component

@Component
class MarketFinder(
    private val marketRepository: MarketRepository,
) {
    fun findNearby(condition: NearbyMarketSearchCondition): CursorSliceResult<NearbyMarket> =
        // 다음 페이지 존재 여부를 알기 위해 한 건 더 조회한다.
        marketRepository
            .findNearby(condition, condition.size + 1)
            .toCursorSlice(condition.size) { it.market.id }
}
