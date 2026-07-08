package kr.dongchimi.core.product

import kr.dongchimi.core.common.exception.CoreException
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class ProductValidator(
    private val productRepository: ProductRepository,
) {
    fun validateBelongsToMarket(
        product: Product,
        marketId: Long,
    ) {
        if (product.marketId != marketId) {
            throw CoreException(ProductErrorCode.PRODUCT_NOT_FOUND)
        }
    }

    fun validateAllInMarket(
        productIds: List<Long>,
        marketId: Long,
    ) {
        if (productRepository.countProductsInMarket(productIds, marketId) != productIds.size) {
            throw CoreException(ProductErrorCode.PRODUCT_NOT_FOUND)
        }
    }

    fun validateAllDiscountEnded(productIds: List<Long>) {
        validateDiscountEnded(productRepository.findAllByIds(productIds))
    }

    fun validateDiscountEndedByDealType(
        marketId: Long,
        dealType: DealType,
    ) {
        validateDiscountEnded(productRepository.findAllByMarketIdAndDealType(marketId, dealType))
    }

    private fun validateDiscountEnded(products: List<Product>) {
        val today = LocalDate.now()
        if (products.any { !it.discountPeriod.isEnded(today) }) {
            throw CoreException(ProductErrorCode.DISCOUNT_NOT_ENDED)
        }
    }
}
