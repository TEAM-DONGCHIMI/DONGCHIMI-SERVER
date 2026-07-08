package kr.dongchimi.core.product

import org.springframework.stereotype.Component

@Component
class ProductUpdater(
    private val productRepository: ProductRepository,
) {
    fun updateDiscountPeriod(
        productIds: List<Long>,
        discountPeriod: DiscountPeriod,
    ) {
        productRepository.updateDiscountPeriod(productIds, discountPeriod)
    }
}
