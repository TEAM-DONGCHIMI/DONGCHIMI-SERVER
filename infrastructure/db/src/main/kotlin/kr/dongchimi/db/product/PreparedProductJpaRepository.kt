package kr.dongchimi.db.product

import kr.dongchimi.core.product.ProductCategory
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface PreparedProductJpaRepository : JpaRepository<PreparedProductJpaEntity, Long> {
    @Query(
        """
        select p from PreparedProductJpaEntity p
        where p.marketId = :marketId
            and p.deletedAt is null
            and (:search is null or p.name like concat('%', cast(:search as string), '%'))
            and (:categories is null or p.category in :categories)
        order by p.createdAt desc
        """,
    )
    fun findDrafts(
        @Param("marketId") marketId: Long,
        @Param("search") search: String?,
        @Param("categories") categories: List<ProductCategory>,
        pageable: Pageable,
    ): List<PreparedProductJpaEntity>

    @Query(
        """
        select
            count(p) as totalCount,
            coalesce(sum(case when p.draftStatus = kr.dongchimi.core.product.DraftStatus.SUCCESS then 1 else 0 end), 0) as successCount,
            coalesce(sum(case when p.draftStatus = kr.dongchimi.core.product.DraftStatus.FAIL then 1 else 0 end), 0) as failCount
        from PreparedProductJpaEntity p
        where p.marketId = :marketId
            and p.deletedAt is null
        """,
    )
    fun countDrafts(
        @Param("marketId") marketId: Long,
    ): PreparedProductDraftCountProjection

    fun findAllByMarketIdAndDeletedAtIsNull(marketId: Long): List<PreparedProductJpaEntity>

    fun findAllByIdInAndDeletedAtIsNull(ids: List<Long>): List<PreparedProductJpaEntity>

    fun countAllByIdInAndMarketIdAndDeletedAtIsNull(
        ids: List<Long>,
        marketId: Long,
    ): Long
}
