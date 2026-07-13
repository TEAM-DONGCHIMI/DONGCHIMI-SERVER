package kr.dongchimi.core.admin

import kr.dongchimi.core.product.ProductCategory

data class DefaultThumbnailUpdateCommand(
    val id: Long,
    val name: String,
    val thumbnailUrl: String,
    val category: ProductCategory,
)
