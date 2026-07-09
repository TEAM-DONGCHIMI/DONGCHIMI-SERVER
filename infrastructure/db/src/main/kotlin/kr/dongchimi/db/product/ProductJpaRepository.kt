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

    fun findAllByIdInAndDeletedAtIsNull(ids: List<Long>): List<ProductJpaEntity>

    fun findAllByMarketIdAndDealTypeAndDeletedAtIsNull(
        marketId: Long,
        dealType: DealType,
    ): List<ProductJpaEntity>

    fun countAllByIdInAndMarketIdAndDeletedAtIsNull(
        productIds: List<Long>,
        marketId: Long,
    ): Long

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

    @Query(
        """
        select p from ProductJpaEntity p
            left join ProductMetadataJpaEntity m on m.id = p.id
        where p.marketId = :marketId
            and p.discountStartDate <= :date
            and p.discountEndDate >= :date
            and p.deletedAt is null
        order by coalesce(m.viewCount, 0) desc, p.createdAt asc
        """,
    )
    fun findPopularActive(
        @Param("marketId") marketId: Long,
        @Param("date") date: LocalDate,
        pageable: Pageable,
    ): List<ProductJpaEntity>
}
