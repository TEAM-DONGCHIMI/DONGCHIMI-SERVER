package kr.dongchimi.core.admin

import kr.dongchimi.core.product.ProductCategory

interface DefaultProductThumbnailRepository {
    fun findById(id: Long): DefaultProductThumbnail?

    fun findAllByCategoryIn(categories: Set<ProductCategory>): List<DefaultProductThumbnail>

    fun save(defaultProductThumbnail: DefaultProductThumbnail): DefaultProductThumbnail
}
