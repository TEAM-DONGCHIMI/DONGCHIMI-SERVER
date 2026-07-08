package kr.dongchimi.core.product

import kr.dongchimi.core.market.MarketValidator
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class ProductService(
    private val marketValidator: MarketValidator,
    private val productReader: ProductReader,
    private val productValidator: ProductValidator,
    private val productRemover: ProductRemover,
    private val productUpdater: ProductUpdater,
) {
    fun getProduct(
        ownerId: Long,
        marketId: Long,
        productId: Long,
    ): Product {
        marketValidator.validateOwnership(marketId, ownerId)

        val product = productReader.read(productId)
        productValidator.validateBelongsToMarket(product, marketId)

        return product
    }

    fun delete(
        ownerId: Long,
        marketId: Long,
        productId: Long,
    ) {
        marketValidator.validateOwnership(marketId, ownerId)

        val product = productReader.read(productId)
        productValidator.validateBelongsToMarket(product, marketId)

        productRemover.remove(productId)
    }

    fun deleteAll(
        ownerId: Long,
        marketId: Long,
        productIds: List<Long>,
    ) {
        marketValidator.validateOwnership(marketId, ownerId)
        productValidator.validateAllInMarket(productIds, marketId)
        productValidator.validateAllDiscountEnded(productIds)

        productRemover.removeAll(productIds)
    }

    fun updateDiscountPeriod(
        ownerId: Long,
        marketId: Long,
        productIds: List<Long>,
        discountPeriod: DiscountPeriod,
    ) {
        marketValidator.validateOwnership(marketId, ownerId)
        productValidator.validateAllInMarket(productIds, marketId)

        productUpdater.updateDiscountPeriod(productIds, discountPeriod)
    }

    fun reset(
        ownerId: Long,
        marketId: Long,
        dealType: DealType,
    ) {
        marketValidator.validateOwnership(marketId, ownerId)
        productValidator.validateDiscountEndedByDealType(marketId, dealType)

        productRemover.resetByDealType(marketId, dealType)
    }

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
