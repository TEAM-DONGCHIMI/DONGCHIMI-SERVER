package kr.dongchimi.db.product

import kr.dongchimi.core.product.DealType
import org.springframework.data.jpa.repository.JpaRepository

interface ProductJpaRepository : JpaRepository<ProductJpaEntity, Long> {
    fun findByIdAndDeletedAtIsNull(id: Long): ProductJpaEntity?

    fun findAllByIdInAndDeletedAtIsNull(ids: List<Long>): List<ProductJpaEntity>

    fun findAllByMarketIdAndDealTypeAndDeletedAtIsNull(
        marketId: Long,
        dealType: DealType,
    ): List<ProductJpaEntity>

    fun countAllByIdInAndMarketIdAndDeletedAtIsNull(
        productIds: List<Long>,
        marketId: Long,
    ): Long
}
