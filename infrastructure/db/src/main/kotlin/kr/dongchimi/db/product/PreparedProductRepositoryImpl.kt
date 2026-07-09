package kr.dongchimi.db.product

import kr.dongchimi.core.common.PageOffset
import kr.dongchimi.core.product.DraftFailReason
import kr.dongchimi.core.product.PreparedProduct
import kr.dongchimi.core.product.PreparedProductDraftCounts
import kr.dongchimi.core.product.PreparedProductDraftSaveCommand
import kr.dongchimi.core.product.PreparedProductRepository
import kr.dongchimi.core.product.PreparedProductSearchCondition
import kr.dongchimi.db.common.toPageRequest
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class PreparedProductRepositoryImpl(
    private val preparedProductJpaRepository: PreparedProductJpaRepository,
) : PreparedProductRepository {
    override fun findDrafts(
        marketId: Long,
        condition: PreparedProductSearchCondition,
        pageOffset: PageOffset,
    ): List<PreparedProduct> =
        preparedProductJpaRepository
            .findDrafts(
                marketId = marketId,
                search = condition.search,
                categories = condition.categories,
                pageable = pageOffset.toPageRequest(),
            ).map { it.toDomain() }

    override fun countDrafts(marketId: Long): PreparedProductDraftCounts {
        val projection = preparedProductJpaRepository.countDrafts(marketId)

        return PreparedProductDraftCounts(
            totalCount = projection.totalCount,
            successCount = projection.successCount,
            failCount = projection.failCount,
        )
    }

    override fun findAllByMarketId(marketId: Long): List<PreparedProduct> =
        preparedProductJpaRepository.findAllByMarketIdAndDeletedAtIsNull(marketId).map { it.toDomain() }

    override fun countInMarket(
        ids: List<Long>,
        marketId: Long,
    ): Int = preparedProductJpaRepository.countAllByIdInAndMarketIdAndDeletedAtIsNull(ids, marketId).toInt()

    @Transactional
    override fun updateDrafts(
        commands: List<PreparedProductDraftSaveCommand>,
        failReasons: Map<Long, DraftFailReason?>,
    ) {
        val entities = preparedProductJpaRepository.findAllByIdInAndDeletedAtIsNull(commands.map { it.id }).associateBy { it.id }

        commands.forEach { command ->
            entities[command.id]?.update(command, failReasons[command.id])
        }
    }

    @Transactional
    override fun softDeleteByIds(ids: List<Long>) {
        val entities = preparedProductJpaRepository.findAllByIdInAndDeletedAtIsNull(ids)

        entities.forEach { it.delete() }
    }
}
