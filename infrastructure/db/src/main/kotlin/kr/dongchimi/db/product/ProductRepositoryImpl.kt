package kr.dongchimi.db.product

import kr.dongchimi.core.product.DealType
import kr.dongchimi.core.product.Product
import kr.dongchimi.core.product.ProductRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
class ProductRepositoryImpl(
    private val productJpaRepository: ProductJpaRepository,
) : ProductRepository {
    override fun findById(id: Long): Product? = productJpaRepository.findByIdAndDeletedAtIsNull(id)?.toDomain()

    override fun save(product: Product): Product = productJpaRepository.save(ProductJpaEntity(product)).toDomain()

    override fun findActiveByMarketIdAndDealType(
        marketId: Long,
        dealType: DealType,
        date: LocalDate,
        limit: Int,
    ): List<Product> =
        productJpaRepository
            .findActive(marketId, dealType, date, PageRequest.of(0, limit))
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
}
