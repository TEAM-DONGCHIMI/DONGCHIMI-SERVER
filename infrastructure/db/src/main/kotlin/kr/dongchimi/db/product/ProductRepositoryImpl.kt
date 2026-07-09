package kr.dongchimi.db.product

import kr.dongchimi.core.product.DealType
import kr.dongchimi.core.product.DiscountPeriod
import kr.dongchimi.core.product.Product
import kr.dongchimi.core.product.ProductRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Repository
class ProductRepositoryImpl(
    private val productJpaRepository: ProductJpaRepository,
) : ProductRepository {
    override fun findById(id: Long): Product? = productJpaRepository.findByIdAndDeletedAtIsNull(id)?.toDomain()

    override fun findAllByIds(ids: List<Long>): List<Product> =
        productJpaRepository.findAllByIdInAndDeletedAtIsNull(ids).map { it.toDomain() }

    override fun findAllByMarketIdAndDealType(
        marketId: Long,
        dealType: DealType,
    ): List<Product> = productJpaRepository.findAllByMarketIdAndDealTypeAndDeletedAtIsNull(marketId, dealType).map { it.toDomain() }

    override fun save(product: Product): Product = productJpaRepository.save(ProductJpaEntity(product)).toDomain()

    override fun saveAll(products: List<Product>): List<Product> =
        productJpaRepository.saveAll(products.map { ProductJpaEntity(it) }).map { it.toDomain() }

    @Transactional
    override fun softDeleteByIds(ids: List<Long>) {
        val entities = productJpaRepository.findAllByIdInAndDeletedAtIsNull(ids)
        entities.forEach { it.delete() }
    }

    @Transactional
    override fun softDeleteByMarketIdAndDealType(
        marketId: Long,
        dealType: DealType,
    ) {
        val entities = productJpaRepository.findAllByMarketIdAndDealTypeAndDeletedAtIsNull(marketId, dealType)
        entities.forEach { it.delete() }
    }

    override fun countProductsInMarket(
        productIds: List<Long>,
        marketId: Long,
    ): Int = productJpaRepository.countAllByIdInAndMarketIdAndDeletedAtIsNull(productIds, marketId).toInt()

    @Transactional
    override fun updateDiscountPeriod(
        productIds: List<Long>,
        discountPeriod: DiscountPeriod,
    ) {
        val products = productJpaRepository.findAllByIdInAndDeletedAtIsNull(productIds)

        products.forEach { it.updateDiscountPeriod(discountPeriod) }
    }

    override fun findActiveByMarketIdAndDealType(
        marketId: Long,
        dealType: DealType,
        date: LocalDate,
        limit: Int,
    ): List<Product> =
        productJpaRepository
            .findActive(marketId, dealType, date, PageRequest.of(0, limit))
            .map { it.toDomain() }

    override fun findAllActiveByMarketIdAndDealType(
        marketId: Long,
        dealType: DealType,
        date: LocalDate,
    ): List<Product> =
        productJpaRepository
            .findAllActive(marketId, dealType, date)
            .map { it.toDomain() }

    override fun countActiveByMarketIdAndDealType(
        marketId: Long,
        dealType: DealType,
        date: LocalDate,
    ): Int = productJpaRepository.countActive(marketId, dealType, date)

    override fun countRegisteredOn(
        marketId: Long,
        date: LocalDate,
    ): Int =
        productJpaRepository.countByMarketIdAndCreatedAtBetweenAndDeletedAtIsNull(
            marketId,
            date.atStartOfDay(),
            date.plusDays(1).atStartOfDay(),
        )

    override fun findPopularActive(
        marketId: Long,
        date: LocalDate,
        limit: Int,
    ): List<Product> =
        productJpaRepository
            .findPopularActive(marketId, date, PageRequest.of(0, limit))
            .map { it.toDomain() }

    override fun findLatestActiveByMarketIds(
        marketIds: List<Long>,
        date: LocalDate,
        limitPerMarket: Int,
    ): List<Product> = productJpaRepository.findLatestActiveByMarketIds(marketIds, date, limitPerMarket).map { it.toDomain() }

    override fun countActiveByMarketIds(
        marketIds: List<Long>,
        date: LocalDate,
    ): Map<Long, Int> =
        productJpaRepository
            .countActiveByMarketIds(marketIds, date)
            .associate { it.marketId to it.productCount.toInt() }
}
