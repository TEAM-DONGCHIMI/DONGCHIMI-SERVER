package kr.dongchimi.core.market

import kr.dongchimi.core.product.Product
import kr.dongchimi.core.product.ProductRepository
import java.time.LocalDate

class ProductFinder(
    private val productRepository: ProductRepository,
) {
    fun findPopularActive(
        marketId: Long,
        date: LocalDate,
        limit: Int,
    ): List<Product> = productRepository.findPopularActive(marketId, date, limit)
}
