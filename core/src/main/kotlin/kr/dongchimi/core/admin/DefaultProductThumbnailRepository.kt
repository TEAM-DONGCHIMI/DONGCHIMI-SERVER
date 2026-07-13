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

    fun saveAll(defaultProductThumbnails: List<DefaultProductThumbnail>): List<DefaultProductThumbnail>

    /** name/thumbnailUrl/category만 갱신한다(id/createdBy/createdAt은 건드리지 않음). 갱신된 행 수를 반환한다. */
    fun updateContent(
        id: Long,
        name: String,
        thumbnailUrl: String,
        category: ProductCategory,
    ): Int
}
