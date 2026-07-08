package kr.dongchimi.core.product

import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class ProductService(
    private val productReader: ProductReader,
) {
    fun getActiveProducts(
        marketId: Long,
        dealType: DealType,
        date: LocalDate,
        limit: Int,
    ): List<Product> = productReader.readActive(marketId, dealType, date, limit)

    fun countActiveProducts(
        marketId: Long,
        dealType: DealType,
        date: LocalDate,
    ): Int = productReader.countActive(marketId, dealType, date)

    fun countRegisteredOn(
        marketId: Long,
        date: LocalDate,
    ): Int = productReader.countRegisteredOn(marketId, date)
}
