package kr.dongchimi.db.product

import kr.dongchimi.core.product.ProductCategory
import kr.dongchimi.core.product.ProductListItem

class OwnerProductRow(
    val product: ProductJpaEntity,
    val viewCount: Int,
) {
    fun toListItem(): ProductListItem = ProductListItem(product.toDomain(), viewCount, product.createdAt)
}

class OwnerProductAnchorRow(
    val category: ProductCategory,
    val viewCount: Int,
)
