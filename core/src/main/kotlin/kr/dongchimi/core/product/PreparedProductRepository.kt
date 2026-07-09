package kr.dongchimi.core.product

import kr.dongchimi.core.common.PageOffset

interface PreparedProductRepository {
    fun findDrafts(
        marketId: Long,
        condition: PreparedProductSearchCondition,
        pageOffset: PageOffset,
    ): List<PreparedProduct>

    fun countDrafts(marketId: Long): PreparedProductDraftCounts
}
