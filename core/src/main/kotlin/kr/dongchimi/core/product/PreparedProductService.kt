package kr.dongchimi.core.product

import kr.dongchimi.core.common.PageOffset
import kr.dongchimi.core.market.MarketValidator
import kr.dongchimi.core.upload.UploadService
import org.springframework.stereotype.Service

@Service
class PreparedProductService(
    private val marketValidator: MarketValidator,
    private val preparedProductFinder: PreparedProductFinder,
    private val preparedProductValidator: PreparedProductValidator,
    private val preparedProductUpdater: PreparedProductUpdater,
    private val preparedProductConfirmer: PreparedProductConfirmer,
    private val uploadService: UploadService,
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

        preparedProductUpdater.syncDrafts(marketId, commands)
    }

    fun confirmDrafts(
        ownerId: Long,
        marketId: Long,
    ) {
        marketValidator.validateOwnership(marketId, ownerId)

        val drafts = preparedProductFinder.findAllByMarketId(marketId)
        uploadService.withConfirmRollback { confirm ->
            val confirmedDrafts = drafts.map { it.copy(thumbnailUrl = it.thumbnailUrl?.let(confirm)) }
            preparedProductConfirmer.confirm(confirmedDrafts)
        }
    }

    fun getPreviewDrafts(
        ownerId: Long,
        marketId: Long,
    ): List<PreparedProduct> {
        marketValidator.validateOwnership(marketId, ownerId)
        return preparedProductFinder.findAllByMarketIdAndDraftStatus(marketId, DraftStatus.SUCCESS)
    }
}
