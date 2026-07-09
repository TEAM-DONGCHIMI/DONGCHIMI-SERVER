package kr.dongchimi.core.product

import kr.dongchimi.core.common.PageOffset
import kr.dongchimi.core.market.MarketValidator
import org.springframework.stereotype.Service

@Service
class PreparedProductService(
    private val marketValidator: MarketValidator,
    private val preparedProductFinder: PreparedProductFinder,
) {
    fun getDrafts(
        ownerId: Long,
        marketId: Long,
        condition: PreparedProductSearchCondition,
        pageOffset: PageOffset,
    ): List<PreparedProduct> {
        marketValidator.validateOwnership(marketId, ownerId)

        return preparedProductFinder.findDrafts(marketId, condition, pageOffset)
    }

    fun getDraftCounts(
        ownerId: Long,
        marketId: Long,
    ): PreparedProductDraftCounts {
        marketValidator.validateOwnership(marketId, ownerId)

        return preparedProductFinder.countDrafts(marketId)
    }
}
