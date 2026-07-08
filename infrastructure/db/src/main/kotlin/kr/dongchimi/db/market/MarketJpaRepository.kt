package kr.dongchimi.db.market

import org.springframework.data.jpa.repository.JpaRepository

interface MarketJpaRepository : JpaRepository<MarketJpaEntity, Long> {
    fun findByIdAndDeletedAtIsNull(id: Long): MarketJpaEntity?

    fun existsByOwnerIdAndNameAndDeletedAtIsNull(
        ownerId: Long,
        name: String,
    ): Boolean

    fun existsByOwnerIdAndNameAndIdNotAndDeletedAtIsNull(
        ownerId: Long,
        name: String,
        id: Long,
    ): Boolean

    fun existsByIdAndOwnerIdAndDeletedAtIsNull(
        marketId: Long,
        ownerId: Long,
    ): Boolean

    fun existsByIdAndDeletedAtIsNull(id: Long): Boolean
}
