package kr.dongchimi.core.product

import kr.dongchimi.core.common.PageOffset
import kr.dongchimi.core.market.MarketValidator
import org.springframework.stereotype.Service

@Service
class PreparedProductService(
    private val marketValidator: MarketValidator,
    private val preparedProductFinder: PreparedProductFinder,
    private val preparedProductValidator: PreparedProductValidator,
    private val preparedProductUpdater: PreparedProductUpdater,
    private val preparedProductConfirmer: PreparedProductConfirmer,
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

    fun saveDrafts(
        ownerId: Long,
        marketId: Long,
        commands: List<PreparedProductDraftSaveCommand>,
    ) {
        marketValidator.validateOwnership(marketId, ownerId)
        preparedProductValidator.validateAllInMarket(commands.map { it.id }, marketId)

        preparedProductUpdater.updateDrafts(commands)
    }

    fun confirmDrafts(
        ownerId: Long,
        marketId: Long,
    ) {
        marketValidator.validateOwnership(marketId, ownerId)

        val drafts = preparedProductFinder.findAllByMarketId(marketId)
        preparedProductValidator.validateAllCompleted(drafts)

        preparedProductConfirmer.confirm(drafts)
    }
}
