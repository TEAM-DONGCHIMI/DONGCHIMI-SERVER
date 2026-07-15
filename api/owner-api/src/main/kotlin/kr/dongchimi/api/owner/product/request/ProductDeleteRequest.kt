package kr.dongchimi.api.owner.product.request

import io.swagger.v3.oas.annotations.media.Schema

data class ProductDeleteRequest(
    @Schema(description = "할인 기간 중이어도 삭제할지 여부", example = "false")
    val forceDelete: Boolean = false,
)
