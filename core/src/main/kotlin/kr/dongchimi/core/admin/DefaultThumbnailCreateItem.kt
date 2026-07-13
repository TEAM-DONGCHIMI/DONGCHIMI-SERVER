package kr.dongchimi.core.admin

import kr.dongchimi.core.product.ProductCategory

data class DefaultThumbnailCreateItem(
    val name: String,
    val thumbnailUrl: String,
    val category: ProductCategory,
)
