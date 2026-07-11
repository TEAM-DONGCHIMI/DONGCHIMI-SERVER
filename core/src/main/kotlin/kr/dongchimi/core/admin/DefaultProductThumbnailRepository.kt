package kr.dongchimi.core.admin

import kr.dongchimi.core.product.ProductCategory

interface DefaultProductThumbnailRepository {
    fun findById(id: Long): DefaultProductThumbnail?

    fun findAllByCategoryIn(categories: Set<ProductCategory>): List<DefaultProductThumbnail>

    fun findAllByLatest(
        search: String?,
        cursor: Long?,
        limit: Int,
    ): List<DefaultProductThumbnail>

    fun findAllByName(
        search: String?,
        cursor: Long?,
        cursorName: String?,
        limit: Int,
    ): List<DefaultProductThumbnail>

    fun findNameById(id: Long): String?

    fun save(defaultProductThumbnail: DefaultProductThumbnail): DefaultProductThumbnail
}
