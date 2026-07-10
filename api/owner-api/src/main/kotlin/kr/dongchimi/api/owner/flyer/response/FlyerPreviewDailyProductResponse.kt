package kr.dongchimi.api.owner.flyer.response

import io.swagger.v3.oas.annotations.media.Schema
import kr.dongchimi.core.product.Product
import java.math.BigDecimal

data class FlyerPreviewDailyProductResponse(
    @Schema(description = "상품 id")
    val productId: Long,
    @Schema(description = "상품명")
    val name: String,
    @Schema(description = "상품 썸네일 이미지 URL")
    val thumbnailUrl: String,
    @Schema(description = "정가")
    val originalPrice: BigDecimal,
    @Schema(description = "할인가")
    val discountedPrice: BigDecimal,
    @Schema(description = "할인율(%)")
    val discountRate: Int,
) {
    constructor(product: Product) : this(
        productId = product.id,
        name = product.name,
        thumbnailUrl = product.thumbnailUrl,
        originalPrice = product.price.originalPrice,
        discountedPrice = product.price.discountedPrice,
        discountRate = product.price.discountRate(),
    )
}
