package kr.dongchimi.core.admin

import kr.dongchimi.core.product.ProductCategory

data class DefaultProductThumbnail(
    val id: Long = 0,
    val name: String,
    val thumbnailUrl: String,
    val category: ProductCategory,
    val createdBy: Long,
)
