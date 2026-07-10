package kr.dongchimi.api.user.market.response

import io.swagger.v3.oas.annotations.media.Schema
import kr.dongchimi.core.product.Product

data class PopularProductResponse(
    @Schema(description = "상품 id")
    val productId: Long,
    @Schema(description = "상품명")
    val name: String,
    @Schema(description = "상품 썸네일 이미지 URL (없으면 null)")
    val thumbnailUrl: String?,
    @Schema(description = "할인가")
    val discountedPrice: Int,
    @Schema(description = "할인율(%). 0이면 할인 없음")
    val discountRate: Int,
) {
    constructor(product: Product) : this(
        productId = product.id,
        name = product.name,
        thumbnailUrl = product.thumbnailUrl,
        discountedPrice = product.price.discountedPrice.toInt(),
        discountRate = product.price.discountRate(),
    )
}
