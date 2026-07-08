package kr.dongchimi.core.product

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class ProductRemover(
    private val productRepository: ProductRepository,
) {
    @Transactional
    fun remove(productId: Long) {
        productRepository.softDeleteByIds(listOf(productId))
    }

    @Transactional
    fun removeAll(productIds: List<Long>) {
        productRepository.softDeleteByIds(productIds)
    }

    @Transactional
    fun resetByDealType(
        marketId: Long,
        dealType: DealType,
    ) {
        productRepository.softDeleteByMarketIdAndDealType(marketId, dealType)
    }
}
