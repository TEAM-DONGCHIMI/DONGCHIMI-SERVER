package kr.dongchimi.api.owner.product.response

import io.swagger.v3.oas.annotations.media.Schema
import kr.dongchimi.core.product.DealType
import kr.dongchimi.core.product.Product
import kr.dongchimi.core.product.ProductCategory
import java.math.BigDecimal
import java.time.LocalDate

data class OwnerProductDetailResponse(
    @Schema(description = "상품 id")
    val productId: Long,
    @Schema(description = "상품명")
    val name: String,
    @Schema(description = "PERIODIC(기간 할인) / DAILY(오늘의 특가)")
    val dealType: DealType,
    @Schema(description = "상품 썸네일 이미지 URL (없으면 null)")
    val thumbnailUrl: String?,
    @Schema(description = "정가")
    val originalPrice: BigDecimal,
    @Schema(description = "할인가")
    val discountedPrice: BigDecimal,
    @Schema(description = "카테고리 코드")
    val category: ProductCategory,
    @Schema(description = "카테고리 표시명")
    val categoryName: String,
    @Schema(description = "홍보 문구 (없으면 null)")
    val promotionalPhrase: String?,
    @Schema(description = "할인 시작일 (YYYY-MM-DD)")
    val discountStartDate: LocalDate,
    @Schema(description = "할인 종료일 (YYYY-MM-DD)")
    val discountEndDate: LocalDate,
) {
    constructor(product: Product) : this(
        productId = product.id,
        name = product.name,
        dealType = product.dealType,
        thumbnailUrl = product.thumbnailUrl,
        originalPrice = product.price.originalPrice,
        discountedPrice = product.price.discountedPrice,
        category = product.category,
        categoryName = product.category.displayName,
        promotionalPhrase = product.promotionalPhrase,
        discountStartDate = product.discountPeriod.discountStartDate,
        discountEndDate = product.discountPeriod.discountEndDate,
    )
}
