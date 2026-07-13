package kr.dongchimi.db.admin

import kr.dongchimi.core.product.ProductCategory
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface DefaultProductThumbnailJpaRepository : JpaRepository<DefaultProductThumbnailJpaEntity, Long> {
    fun findAllByCategoryIn(categories: Set<ProductCategory>): List<DefaultProductThumbnailJpaEntity>

    @Query(
        """
        select p from DefaultProductThumbnailJpaEntity p
        where (:cursor is null or p.id < :cursor)
          and (:search is null or lower(p.name) like concat('%', cast(:search as string), '%'))
        order by p.id desc
        """,
    )
    fun findAllByLatest(
        @Param("cursor") cursor: Long?,
        @Param("search") search: String?,
        pageable: Pageable,
    ): List<DefaultProductThumbnailJpaEntity>

    @Query(
        """
        select p from DefaultProductThumbnailJpaEntity p
        where (:search is null or lower(p.name) like concat('%', cast(:search as string), '%'))
          and (:cursor is null or p.name > :cursorName or (p.name = :cursorName and p.id > :cursor))
        order by p.name asc, p.id asc
        """,
    )
    fun findAllByName(
        @Param("cursor") cursor: Long?,
        @Param("cursorName") cursorName: String?,
        @Param("search") search: String?,
        pageable: Pageable,
    ): List<DefaultProductThumbnailJpaEntity>

    @Query("select p.name from DefaultProductThumbnailJpaEntity p where p.id = :id")
    fun findNameById(
        @Param("id") id: Long,
    ): String?
}
