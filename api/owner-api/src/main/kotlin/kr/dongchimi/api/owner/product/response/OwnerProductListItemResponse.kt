package kr.dongchimi.api.owner.product.response

import io.swagger.v3.oas.annotations.media.Schema
import kr.dongchimi.core.product.Product
import kr.dongchimi.core.product.ProductCategory
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

data class OwnerProductListItemResponse(
    @Schema(description = "상품 id")
    val productId: Long,
    @Schema(description = "상품명")
    val name: String,
    @Schema(description = "상품 썸네일 이미지 URL (없으면 null)")
    val thumbnailUrl: String?,
    @Schema(description = "카테고리 코드")
    val category: ProductCategory,
    @Schema(description = "카테고리 표시명")
    val categoryName: String,
    @Schema(description = "정가")
    val originalPrice: BigDecimal,
    @Schema(description = "할인가")
    val discountedPrice: BigDecimal,
    @Schema(description = "홍보 문구 (없으면 null)")
    val promotionalPhrase: String?,
    @Schema(description = "할인 시작일")
    val discountStartDate: LocalDate,
    @Schema(description = "할인 종료일")
    val discountEndDate: LocalDate,
    @Schema(description = "조회수")
    val viewCount: Int,
    @Schema(description = "상품 등록 일시")
    val createdAt: LocalDateTime,
) {
    constructor(product: Product, viewCount: Int, createdAt: LocalDateTime) : this(
        productId = product.id,
        name = product.name,
        thumbnailUrl = product.thumbnailUrl,
        category = product.category,
        categoryName = product.category.displayName,
        originalPrice = product.price.originalPrice,
        discountedPrice = product.price.discountedPrice,
        promotionalPhrase = product.promotionalPhrase,
        discountStartDate = product.discountPeriod.discountStartDate,
        discountEndDate = product.discountPeriod.discountEndDate,
        viewCount = viewCount,
        createdAt = createdAt,
    )
}
