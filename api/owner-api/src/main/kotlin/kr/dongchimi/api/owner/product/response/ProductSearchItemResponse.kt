package kr.dongchimi.api.owner.product.response

import io.swagger.v3.oas.annotations.media.Schema
import kr.dongchimi.core.product.DealType
import kr.dongchimi.core.product.Product

data class ProductSearchItemResponse(
    @Schema(description = "상품 id")
    val productId: Long,
    @Schema(description = "상품명")
    val name: String,
    @Schema(description = "판매 유형")
    val dealType: DealType,
) {
    constructor(product: Product) : this(
        productId = product.id,
        name = product.name,
        dealType = product.dealType,
    )
}
