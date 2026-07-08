package kr.dongchimi.db.market

import org.springframework.data.jpa.repository.JpaRepository

interface MarketJpaRepository : JpaRepository<MarketJpaEntity, Long> {
    fun findByIdAndDeletedAtIsNull(id: Long): MarketJpaEntity?

    fun findByOwnerIdAndDeletedAtIsNull(ownerId: Long): MarketJpaEntity?
}
