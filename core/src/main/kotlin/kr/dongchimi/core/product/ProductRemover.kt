package kr.dongchimi.core.product

import org.springframework.stereotype.Component

@Component
class ProductRemover(
    private val productRepository: ProductRepository,
) {
    fun remove(productId: Long) {
        productRepository.softDeleteByIds(listOf(productId))
    }

    fun removeAll(productIds: List<Long>) {
        productRepository.softDeleteByIds(productIds)
    }

    fun resetByDealType(
        marketId: Long,
        dealType: DealType,
    ) {
        productRepository.softDeleteByMarketIdAndDealType(marketId, dealType)
    }
}
