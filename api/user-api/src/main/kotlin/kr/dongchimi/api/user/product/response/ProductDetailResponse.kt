package kr.dongchimi.api.user.product.response

import io.swagger.v3.oas.annotations.media.Schema
import kr.dongchimi.core.product.DealType
import kr.dongchimi.core.product.Product
import java.math.BigDecimal
import java.time.LocalDate

data class ProductDetailResponse(
    @Schema(description = "상품 id")
    val productId: Long,
    @Schema(description = "상품명")
    val name: String,
    @Schema(description = "PERIODIC(기간 할인) / DAILY(오늘의 특가)")
    val dealType: DealType,
    @Schema(description = "상품 썸네일 이미지 URL")
    val thumbnailUrl: String?,
    @Schema(description = "정가")
    val originalPrice: BigDecimal,
    @Schema(description = "할인가")
    val discountedPrice: BigDecimal,
    @Schema(description = "할인율(%)")
    val discountRate: Int,
    @Schema(description = "홍보 문구 (없으면 null)")
    val promotionalPhrase: String?,
    @Schema(description = "할인 시작일 (YYYY-MM-DD)")
    val discountStartDate: LocalDate,
    @Schema(description = "할인 종료일 (YYYY-MM-DD)")
    val discountEndDate: LocalDate,
    @Schema(description = "마트 이름")
    val marketName: String,
) {
    constructor(product: Product, marketName: String) : this(
        productId = product.id,
        name = product.name,
        dealType = product.dealType,
        thumbnailUrl = product.thumbnailUrl,
        originalPrice = product.price.originalPrice,
        discountedPrice = product.price.discountedPrice,
        discountRate = product.price.discountRate(),
        promotionalPhrase = product.promotionalPhrase,
        discountStartDate = product.discountPeriod.discountStartDate,
        discountEndDate = product.discountPeriod.discountEndDate,
        marketName = marketName,
    )
}
