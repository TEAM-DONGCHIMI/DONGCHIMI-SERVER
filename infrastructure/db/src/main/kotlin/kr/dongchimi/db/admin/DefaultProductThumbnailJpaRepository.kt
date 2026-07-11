package kr.dongchimi.db.admin

import kr.dongchimi.core.product.ProductCategory
import org.springframework.data.jpa.repository.JpaRepository

interface DefaultProductThumbnailJpaRepository : JpaRepository<DefaultProductThumbnailJpaEntity, Long> {
    fun findAllByCategoryIn(categories: Set<ProductCategory>): List<DefaultProductThumbnailJpaEntity>
}
