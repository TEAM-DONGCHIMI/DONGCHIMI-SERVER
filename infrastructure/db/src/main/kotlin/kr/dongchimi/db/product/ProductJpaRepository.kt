package kr.dongchimi.db.product

import kr.dongchimi.core.product.DealType
import kr.dongchimi.core.product.ProductCategory
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

    @Query(
        """
        select p from ProductJpaEntity p
        where p.marketId = :marketId
            and p.dealType = :dealType
            and p.discountStartDate <= :date
            and p.discountEndDate >= :date
            and p.deletedAt is null
            and (:category is null or p.category = :category)
            and (:cursor is null or p.id < :cursor)
        order by p.id desc
        """,
    )
    fun findActiveByCategory(
        @Param("marketId") marketId: Long,
        @Param("dealType") dealType: DealType,
        @Param("category") category: ProductCategory?,
        @Param("cursor") cursor: Long?,
        @Param("date") date: LocalDate,
        pageable: Pageable,
    ): List<ProductJpaEntity>

    @Query(
        """
        select new kr.dongchimi.db.product.OwnerProductRow(p, coalesce(m.viewCount, 0))
        from ProductJpaEntity p
            left join ProductMetadataJpaEntity m on m.id = p.id
        where p.marketId = :marketId
            and p.dealType = :dealType
            and p.discountStartDate <= :date
            and p.discountEndDate >= :date
            and p.deletedAt is null
            and (:category is null or p.category = :category)
            and (:cursor is null or p.id < :cursor)
        order by p.id desc
        """,
    )
    fun findActiveByLatest(
        @Param("marketId") marketId: Long,
        @Param("dealType") dealType: DealType,
        @Param("category") category: ProductCategory?,
        @Param("cursor") cursor: Long?,
        @Param("date") date: LocalDate,
        pageable: Pageable,
    ): List<OwnerProductRow>

    @Query(
        """
        select new kr.dongchimi.db.product.OwnerProductRow(p, coalesce(m.viewCount, 0))
        from ProductJpaEntity p
            left join ProductMetadataJpaEntity m on m.id = p.id
        where p.marketId = :marketId
            and p.dealType = :dealType
            and p.discountStartDate <= :date
            and p.discountEndDate >= :date
            and p.deletedAt is null
            and (:category is null or p.category = :category)
            and (:cursor is null or
                 coalesce(m.viewCount, 0) < :cursorViewCount or
                 (coalesce(m.viewCount, 0) = :cursorViewCount and p.id < :cursor))
        order by coalesce(m.viewCount, 0) desc, p.id desc
        """,
    )
    fun findActiveByViewCount(
        @Param("marketId") marketId: Long,
        @Param("dealType") dealType: DealType,
        @Param("category") category: ProductCategory?,
        @Param("cursor") cursor: Long?,
        @Param("cursorViewCount") cursorViewCount: Int?,
        @Param("date") date: LocalDate,
        pageable: Pageable,
    ): List<OwnerProductRow>

    // ProductCategory는 STRING 저장이라 order by p.category는 알파벳순이 되어버린다 — 선언순(ordinal) 강제를 위해 CASE로 매핑한다.
    // ProductCategory 선언 순서가 바뀌면 이 CASE 매핑도 함께 바꿔야 한다(ProductCategoryOrderTest가 어긋남을 감지한다).
    @Query(
        """
        select new kr.dongchimi.db.product.OwnerProductRow(p, coalesce(m.viewCount, 0))
        from ProductJpaEntity p
            left join ProductMetadataJpaEntity m on m.id = p.id
        where p.marketId = :marketId
            and p.dealType = :dealType
            and p.discountStartDate <= :date
            and p.discountEndDate >= :date
            and p.deletedAt is null
            and (:category is null or p.category = :category)
            and (:cursor is null or
                 case p.category
                     when kr.dongchimi.core.product.ProductCategory.VEGETABLE_FRUIT then 0
                     when kr.dongchimi.core.product.ProductCategory.MEAT_EGG then 1
                     when kr.dongchimi.core.product.ProductCategory.SEAFOOD then 2
                     when kr.dongchimi.core.product.ProductCategory.DAIRY then 3
                     when kr.dongchimi.core.product.ProductCategory.CONVENIENCE_FOOD then 4
                     when kr.dongchimi.core.product.ProductCategory.PROCESSED_FOOD then 5
                     when kr.dongchimi.core.product.ProductCategory.BEVERAGE_ALCOHOL then 6
                     when kr.dongchimi.core.product.ProductCategory.HOUSEHOLD_GOODS then 7
                     else 8
                 end > :cursorCategoryOrder or
                 (case p.category
                     when kr.dongchimi.core.product.ProductCategory.VEGETABLE_FRUIT then 0
                     when kr.dongchimi.core.product.ProductCategory.MEAT_EGG then 1
                     when kr.dongchimi.core.product.ProductCategory.SEAFOOD then 2
                     when kr.dongchimi.core.product.ProductCategory.DAIRY then 3
                     when kr.dongchimi.core.product.ProductCategory.CONVENIENCE_FOOD then 4
                     when kr.dongchimi.core.product.ProductCategory.PROCESSED_FOOD then 5
                     when kr.dongchimi.core.product.ProductCategory.BEVERAGE_ALCOHOL then 6
                     when kr.dongchimi.core.product.ProductCategory.HOUSEHOLD_GOODS then 7
                     else 8
                 end = :cursorCategoryOrder and p.id < :cursor))
        order by
            case p.category
                when kr.dongchimi.core.product.ProductCategory.VEGETABLE_FRUIT then 0
                when kr.dongchimi.core.product.ProductCategory.MEAT_EGG then 1
                when kr.dongchimi.core.product.ProductCategory.SEAFOOD then 2
                when kr.dongchimi.core.product.ProductCategory.DAIRY then 3
                when kr.dongchimi.core.product.ProductCategory.CONVENIENCE_FOOD then 4
                when kr.dongchimi.core.product.ProductCategory.PROCESSED_FOOD then 5
                when kr.dongchimi.core.product.ProductCategory.BEVERAGE_ALCOHOL then 6
                when kr.dongchimi.core.product.ProductCategory.HOUSEHOLD_GOODS then 7
                else 8
            end asc,
            p.id desc
        """,
    )
    fun findActiveByCategoryOrder(
        @Param("marketId") marketId: Long,
        @Param("dealType") dealType: DealType,
        @Param("category") category: ProductCategory?,
        @Param("cursor") cursor: Long?,
        @Param("cursorCategoryOrder") cursorCategoryOrder: Int?,
        @Param("date") date: LocalDate,
        pageable: Pageable,
    ): List<OwnerProductRow>

    @Query(
        """
        select new kr.dongchimi.db.product.OwnerProductAnchorRow(p.category, coalesce(m.viewCount, 0))
        from ProductJpaEntity p
            left join ProductMetadataJpaEntity m on m.id = p.id
        where p.id = :cursor
            and p.deletedAt is null
            and p.marketId = :marketId
        """,
    )
    fun findListCursorAnchor(
        @Param("cursor") cursor: Long,
        @Param("marketId") marketId: Long,
    ): OwnerProductAnchorRow?
}
