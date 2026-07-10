package kr.dongchimi.api.owner.flyer.response

import io.swagger.v3.oas.annotations.media.Schema
import kr.dongchimi.core.product.PreparedProduct
import java.math.BigDecimal

data class FlyerPreviewPreparedProductResponse(
    @Schema(description = "임시저장 상품 id")
    val preparedProductId: Long,
    @Schema(description = "상품명")
    val name: String,
    @Schema(description = "상품 썸네일 이미지 URL")
    val thumbnailUrl: String,
    @Schema(description = "할인가")
    val discountedPrice: BigDecimal,
) {
    // getPreviewDrafts()가 draftStatus=SUCCESS만 반환하므로 PreparedProduct.toProduct()와 동일한 전제로 non-null 단언한다.
    constructor(preparedProduct: PreparedProduct) : this(
        preparedProductId = preparedProduct.id,
        name = preparedProduct.name!!,
        thumbnailUrl = preparedProduct.thumbnailUrl!!,
        discountedPrice = preparedProduct.price!!.discountedPrice,
    )
}
