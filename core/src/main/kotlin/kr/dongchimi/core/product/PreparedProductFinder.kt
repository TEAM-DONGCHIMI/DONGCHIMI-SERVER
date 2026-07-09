package kr.dongchimi.core.product

import kr.dongchimi.core.common.PageOffset
import org.springframework.stereotype.Component

@Component
class PreparedProductFinder(
    private val preparedProductRepository: PreparedProductRepository,
) {
    fun findDrafts(
        marketId: Long,
        condition: PreparedProductSearchCondition,
        pageOffset: PageOffset,
    ): List<PreparedProduct> = preparedProductRepository.findDrafts(marketId, condition, pageOffset)

    fun countDrafts(marketId: Long): PreparedProductDraftCounts = preparedProductRepository.countDrafts(marketId)
}
