package kr.dongchimi.core.product

import kr.dongchimi.core.common.CursorSliceResult
import kr.dongchimi.core.common.exception.CoreException
import kr.dongchimi.core.market.MarketValidator
import kr.dongchimi.core.market.ProductFinder
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class ProductService(
    private val marketValidator: MarketValidator,
    private val productReader: ProductReader,
    private val productValidator: ProductValidator,
    private val productRemover: ProductRemover,
    private val productUpdater: ProductUpdater,
    private val productFinder: ProductFinder,
    private val productAppender: ProductAppender,
) {
    fun registerDailyProduct(
        ownerId: Long,
        marketId: Long,
        command: DailyProductRegisterCommand,
        today: LocalDate,
    ) {
        marketValidator.validateOwnership(marketId, ownerId)

        if (!command.discountPeriod.includes(today)) {
            throw CoreException(ProductErrorCode.INVALID_DISCOUNT_PERIOD)
        }

        productAppender.append(command.toProduct(marketId))
    }

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

    fun updateProduct(
        ownerId: Long,
        marketId: Long,
        productId: Long,
        command: ProductUpdateCommand,
        today: LocalDate,
    ) {
        marketValidator.validateOwnership(marketId, ownerId)

        val product = productReader.read(productId)
        productValidator.validateBelongsToMarket(product, marketId)

        if (command.dealType != product.dealType) {
            throw CoreException(ProductErrorCode.TYPE_MISMATCH)
        }
        if (command.dealType == DealType.DAILY && !command.discountPeriod.includes(today)) {
            throw CoreException(ProductErrorCode.INVALID_DISCOUNT_PERIOD)
        }

        productUpdater.update(command.applyTo(product))
    }

    fun getDetail(
        marketId: Long,
        productId: Long,
    ): Product {
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

    fun getAllActiveProducts(
        marketId: Long,
        dealType: DealType,
        date: LocalDate,
    ): List<Product> {
        marketValidator.validateExists(marketId)

        return productReader.readAllActive(marketId, dealType, date)
    }

    fun countActiveProducts(
        marketId: Long,
        dealType: DealType,
        date: LocalDate,
    ): Int = productReader.countActive(marketId, dealType, date)

    fun countRegisteredOn(
        marketId: Long,
        date: LocalDate,
    ): Int = productReader.countRegisteredOn(marketId, date)

    fun getPopularActiveProducts(
        marketId: Long,
        date: LocalDate,
        limit: Int,
    ): List<Product> = productFinder.findPopularActive(marketId, date, limit)

    fun getLatestActiveProducts(
        marketIds: List<Long>,
        date: LocalDate,
        limitPerMarket: Int,
    ): List<Product> = productFinder.findLatestActiveByMarketIds(marketIds, date, limitPerMarket)

    fun countActiveProductsByMarketIds(
        marketIds: List<Long>,
        date: LocalDate,
    ): Map<Long, Int> = productFinder.countActiveByMarketIds(marketIds, date)

    fun getActiveProductsByCategory(
        marketId: Long,
        dealType: DealType,
        condition: PeriodicProductSearchCondition,
        date: LocalDate,
    ): CursorSliceResult<Product> {
        marketValidator.validateExists(marketId)

        return productFinder.findActiveByCategory(marketId, dealType, condition, date)
    }

    fun getOwnerProducts(
        ownerId: Long,
        marketId: Long,
        condition: ProductListSearchCondition,
        date: LocalDate,
    ): CursorSliceResult<ProductListItem> {
        marketValidator.validateOwnership(marketId, ownerId)

        return productFinder.findActiveProductList(marketId, condition, date)
    }

    fun search(
        ownerId: Long,
        marketId: Long,
        condition: ProductKeywordSearchCondition,
        date: LocalDate,
    ): List<Product> {
        marketValidator.validateOwnership(marketId, ownerId)

        return productFinder.searchByKeyword(marketId, condition, date)
    }
}
