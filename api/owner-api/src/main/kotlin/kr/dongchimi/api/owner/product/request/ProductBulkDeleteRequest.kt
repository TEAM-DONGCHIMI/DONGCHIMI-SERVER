package kr.dongchimi.api.owner.product.request

import io.swagger.v3.oas.annotations.media.Schema

data class ProductBulkDeleteRequest(
    @Schema(description = "삭제할 상품 id 목록", example = "[1, 2, 3]")
    val productIds: List<Long>,
)
