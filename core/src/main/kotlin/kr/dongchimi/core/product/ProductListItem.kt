package kr.dongchimi.core.product

import java.time.LocalDateTime

data class ProductListItem(
    val product: Product,
    val viewCount: Int,
    val createdAt: LocalDateTime,
)
