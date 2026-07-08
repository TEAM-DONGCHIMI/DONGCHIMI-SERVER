package kr.dongchimi.db.product

import kr.dongchimi.core.product.DealType
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDate
import java.time.LocalDateTime

interface ProductJpaRepository : JpaRepository<ProductJpaEntity, Long> {
    fun findByIdAndDeletedAtIsNull(id: Long): ProductJpaEntity?

    @Query(
        """
        select p from ProductJpaEntity p
        where p.marketId = :marketId
            and p.dealType = :dealType
            and p.discountStartDate <= :date
            and p.discountEndDate >= :date
            and p.deletedAt is null
        order by p.createdAt desc
        """,
    )
    fun findActive(
        @Param("marketId") marketId: Long,
        @Param("dealType") dealType: DealType,
        @Param("date") date: LocalDate,
        pageable: Pageable,
    ): List<ProductJpaEntity>

    @Query(
        """
        select count(p) from ProductJpaEntity p
        where p.marketId = :marketId
            and p.dealType = :dealType
            and p.discountStartDate <= :date
            and p.discountEndDate >= :date
            and p.deletedAt is null
        """,
    )
    fun countActive(
        @Param("marketId") marketId: Long,
        @Param("dealType") dealType: DealType,
        @Param("date") date: LocalDate,
    ): Int

    fun countByMarketIdAndCreatedAtBetweenAndDeletedAtIsNull(
        marketId: Long,
        start: LocalDateTime,
        end: LocalDateTime,
    ): Int
}
