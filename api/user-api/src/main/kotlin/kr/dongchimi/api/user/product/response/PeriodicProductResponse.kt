package kr.dongchimi.api.user.product.response

import io.swagger.v3.oas.annotations.media.Schema
import kr.dongchimi.core.product.Product
import java.math.BigDecimal

data class PeriodicProductResponse(
    @Schema(description = "상품 id")
    val productId: Long,
    @Schema(description = "상품명")
    val name: String,
    @Schema(description = "상품 썸네일 이미지 URL")
    val thumbnailUrl: String?,
    @Schema(description = "할인가")
    val discountedPrice: BigDecimal,
) {
    constructor(product: Product) : this(
        productId = product.id,
        name = product.name,
        thumbnailUrl = product.thumbnailUrl,
        discountedPrice = product.price.discountedPrice,
    )
}
