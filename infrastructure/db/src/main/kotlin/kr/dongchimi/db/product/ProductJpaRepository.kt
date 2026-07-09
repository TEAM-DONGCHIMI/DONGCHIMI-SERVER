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
        select p from ProductJpaEntity p
        where p.marketId = :marketId
            and p.dealType = :dealType
            and p.discountStartDate <= :date
            and p.discountEndDate >= :date
            and p.deletedAt is null
        order by p.createdAt desc
        """,
    )
    fun findAllActive(
        @Param("marketId") marketId: Long,
        @Param("dealType") dealType: DealType,
        @Param("date") date: LocalDate,
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

    /**
     * 마트별 최신 활성 상품을 limitPerMarket개씩 한 번에 조회한다.
     * window function은 JPQL로 표현할 수 없어 native로 작성하고, rn이 엔티티 매핑에 섞이지 않도록 바깥에서 컬럼을 명시한다.
     */
    @Query(
        value = """
            SELECT t.product_id, t.market_id, t.name, t.deal_type, t.thumbnail_url,
                   t.original_price, t.discounted_price, t.category, t.promotional_phrase,
                   t.discount_start_date, t.discount_end_date,
                   t.created_at, t.updated_at, t.deleted_at
            FROM (
                SELECT p.*,
                       ROW_NUMBER() OVER (
                           PARTITION BY p.market_id ORDER BY p.created_at DESC, p.product_id DESC
                       ) AS rn
                FROM products p
                WHERE p.market_id IN (:marketIds)
                  AND p.deleted_at IS NULL
                  AND p.discount_start_date <= :date
                  AND p.discount_end_date >= :date
            ) t
            WHERE t.rn <= :limitPerMarket
        """,
        nativeQuery = true,
    )
    fun findLatestActiveByMarketIds(
        @Param("marketIds") marketIds: List<Long>,
        @Param("date") date: LocalDate,
        @Param("limitPerMarket") limitPerMarket: Int,
    ): List<ProductJpaEntity>

    @Query(
        """
        select p.marketId as marketId, count(p) as productCount
        from ProductJpaEntity p
        where p.marketId in :marketIds
            and p.discountStartDate <= :date
            and p.discountEndDate >= :date
            and p.deletedAt is null
        group by p.marketId
        """,
    )
    fun countActiveByMarketIds(
        @Param("marketIds") marketIds: List<Long>,
        @Param("date") date: LocalDate,
    ): List<MarketProductCountProjection>
}
