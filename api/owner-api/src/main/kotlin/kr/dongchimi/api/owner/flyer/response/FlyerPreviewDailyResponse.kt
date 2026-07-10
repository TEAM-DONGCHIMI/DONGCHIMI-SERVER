package kr.dongchimi.api.owner.flyer.response

import io.swagger.v3.oas.annotations.media.Schema
import kr.dongchimi.core.product.Product

data class FlyerPreviewDailyResponse(
    @Schema(description = "오늘의 특가 상품 총 개수 (= products 길이)")
    val totalCount: Int,
    @Schema(description = "오늘의 특가 상품 전체 목록 (최근 등록순)")
    val products: List<FlyerPreviewDailyProductResponse>,
) {
    constructor(products: List<Product>) : this(
        totalCount = products.size,
        products = products.map { FlyerPreviewDailyProductResponse(it) },
    )
}
