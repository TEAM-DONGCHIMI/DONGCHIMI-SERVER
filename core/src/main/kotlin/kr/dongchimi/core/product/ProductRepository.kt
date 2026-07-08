package kr.dongchimi.core.product

import java.time.LocalDate

interface ProductRepository {
    fun findById(id: Long): Product?

    fun save(product: Product): Product

    fun findActiveByMarketIdAndDealType(
        marketId: Long,
        dealType: DealType,
        date: LocalDate,
        limit: Int,
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
}
