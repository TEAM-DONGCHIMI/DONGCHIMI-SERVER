package kr.dongchimi.core.product

import kr.dongchimi.core.common.PageOffset

interface PreparedProductRepository {
    fun findDrafts(
        marketId: Long,
        condition: PreparedProductSearchCondition,
        pageOffset: PageOffset,
    ): List<PreparedProduct>

    fun countDrafts(marketId: Long): PreparedProductDraftCounts

    fun findAllByMarketId(marketId: Long): List<PreparedProduct>

    fun countInMarket(
        ids: List<Long>,
        marketId: Long,
    ): Int

    fun updateDrafts(
        commands: List<PreparedProductDraftSaveCommand>,
        failReasons: Map<Long, DraftFailReason?>,
    )

    fun softDeleteByIds(ids: List<Long>)
}
