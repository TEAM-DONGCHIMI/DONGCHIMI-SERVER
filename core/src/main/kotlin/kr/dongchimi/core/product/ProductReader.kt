package kr.dongchimi.core.product

import kr.dongchimi.core.common.exception.CoreException
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class ProductReader(
    private val productRepository: ProductRepository,
) {
    fun read(id: Long): Product = productRepository.findById(id) ?: throw CoreException(ProductErrorCode.PRODUCT_NOT_FOUND)

    fun readActive(
        marketId: Long,
        dealType: DealType,
        date: LocalDate,
        limit: Int,
    ): List<Product> = productRepository.findActiveByMarketIdAndDealType(marketId, dealType, date, limit)

    fun readAllActive(
        marketId: Long,
        dealType: DealType,
        date: LocalDate,
    ): List<Product> = productRepository.findAllActiveByMarketIdAndDealType(marketId, dealType, date)

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
