package kr.dongchimi.db.product

import kr.dongchimi.core.product.DealType
import kr.dongchimi.core.product.DiscountPeriod
import kr.dongchimi.core.product.Product
import kr.dongchimi.core.product.ProductRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

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

    override fun softDeleteByIds(ids: List<Long>) {
        val entities = productJpaRepository.findAllByIdInAndDeletedAtIsNull(ids)
        entities.forEach { it.delete() }
        productJpaRepository.saveAll(entities)
    }

    override fun softDeleteByMarketIdAndDealType(
        marketId: Long,
        dealType: DealType,
    ) {
        val entities = productJpaRepository.findAllByMarketIdAndDealTypeAndDeletedAtIsNull(marketId, dealType)
        entities.forEach { it.delete() }
        productJpaRepository.saveAll(entities)
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
}
