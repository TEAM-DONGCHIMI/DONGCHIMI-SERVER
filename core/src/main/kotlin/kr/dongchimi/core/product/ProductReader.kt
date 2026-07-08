package kr.dongchimi.core.product

import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class ProductReader(
    private val productRepository: ProductRepository,
) {
    fun readActive(
        marketId: Long,
        dealType: DealType,
        date: LocalDate,
        limit: Int,
    ): List<Product> = productRepository.findActiveByMarketIdAndDealType(marketId, dealType, date, limit)

    fun countActive(
        marketId: Long,
        dealType: DealType,
        date: LocalDate,
    ): Int = productRepository.countActiveByMarketIdAndDealType(marketId, dealType, date)

    fun countRegisteredOn(
        marketId: Long,
        date: LocalDate,
    ): Int = productRepository.countRegisteredOn(marketId, date)
}
