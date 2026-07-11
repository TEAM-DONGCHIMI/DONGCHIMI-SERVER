package kr.dongchimi.api.owner.product.response

import io.swagger.v3.oas.annotations.media.Schema

data class ProductSearchResponse(
    @Schema(description = "검색된 상품 목록")
    val products: List<ProductSearchItemResponse>,
)
