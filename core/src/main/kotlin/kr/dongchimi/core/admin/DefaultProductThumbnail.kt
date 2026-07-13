package kr.dongchimi.core.admin

import kr.dongchimi.core.product.ProductCategory
import java.time.LocalDateTime

data class DefaultProductThumbnail(
    val id: Long = 0,
    val name: String,
    val thumbnailUrl: String,
    val category: ProductCategory,
    val createdBy: Long,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
)
