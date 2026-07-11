package kr.dongchimi.core.product

import java.time.LocalDate

interface ProductRepository {
    fun findById(id: Long): Product?

    fun findAllByIds(ids: List<Long>): List<Product>

    fun findAllByMarketIdAndDealType(
        marketId: Long,
        dealType: DealType,
    ): List<Product>

    fun save(product: Product): Product

    fun saveAll(products: List<Product>): List<Product>

    fun update(product: Product)

    fun softDeleteByIds(ids: List<Long>)

    fun softDeleteByMarketIdAndDealType(
        marketId: Long,
        dealType: DealType,
    )

    fun countProductsInMarket(
        productIds: List<Long>,
        marketId: Long,
    ): Int

    fun updateDiscountPeriod(
        productIds: List<Long>,
        discountPeriod: DiscountPeriod,
    )

    fun findActiveByMarketIdAndDealType(
        marketId: Long,
        dealType: DealType,
        date: LocalDate,
        limit: Int,
    ): List<Product>

    fun findAllActiveByMarketIdAndDealType(
        marketId: Long,
        dealType: DealType,
        date: LocalDate,
    ): List<Product>

    fun countActiveByMarketIdAndDealType(
        marketId: Long,
        dealType: DealType,
        date: LocalDate,
    ): Int

    fun countRegisteredOn(
        marketId: Long,
        date: LocalDate,
    ): Int

    fun findPopularActive(
        marketId: Long,
        date: LocalDate,
        limit: Int,
    ): List<Product>

    fun findLatestActiveByMarketIds(
        marketIds: List<Long>,
        date: LocalDate,
        limitPerMarket: Int,
    ): List<Product>

    fun countActiveByMarketIds(
        marketIds: List<Long>,
        date: LocalDate,
    ): Map<Long, Int>

    fun findActiveByMarketIdAndDealTypeAndCategory(
        marketId: Long,
        dealType: DealType,
        condition: PeriodicProductSearchCondition,
        date: LocalDate,
        limit: Int,
    ): List<Product>

    fun findActiveByLatest(
        marketId: Long,
        condition: ProductListSearchCondition,
        date: LocalDate,
        limit: Int,
    ): List<ProductListItem>

    fun findActiveByViewCount(
        marketId: Long,
        condition: ProductListSearchCondition,
        date: LocalDate,
        cursorViewCount: Int?,
        limit: Int,
    ): List<ProductListItem>

    fun findActiveByCategoryOrder(
        marketId: Long,
        condition: ProductListSearchCondition,
        date: LocalDate,
        cursorCategoryOrder: Int?,
        limit: Int,
    ): List<ProductListItem>

    fun findListCursorAnchor(
        cursor: Long,
        marketId: Long,
    ): ProductListCursorAnchor?

    fun searchActiveByMarketIdAndKeyword(
        marketId: Long,
        condition: ProductKeywordSearchCondition,
        date: LocalDate,
    ): List<Product>

    fun findAllActiveByMarketId(
        marketId: Long,
        date: LocalDate,
    ): List<Product>
}
