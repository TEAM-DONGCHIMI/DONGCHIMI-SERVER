package kr.dongchimi.db.product

import kr.dongchimi.core.common.PageOffset
import kr.dongchimi.core.product.PreparedProduct
import kr.dongchimi.core.product.PreparedProductDraftCounts
import kr.dongchimi.core.product.PreparedProductRepository
import kr.dongchimi.core.product.PreparedProductSearchCondition
import kr.dongchimi.db.common.toPageRequest
import org.springframework.stereotype.Repository

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
                categories = condition.categories.ifEmpty { null },
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
}
