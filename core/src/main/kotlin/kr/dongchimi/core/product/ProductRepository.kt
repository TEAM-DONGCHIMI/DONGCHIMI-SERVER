package kr.dongchimi.core.product

interface ProductRepository {
    fun findById(id: Long): Product?

    fun findAllByIds(ids: List<Long>): List<Product>

    fun findAllByMarketIdAndDealType(
        marketId: Long,
        dealType: DealType,
    ): List<Product>

    fun save(product: Product): Product

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
}
