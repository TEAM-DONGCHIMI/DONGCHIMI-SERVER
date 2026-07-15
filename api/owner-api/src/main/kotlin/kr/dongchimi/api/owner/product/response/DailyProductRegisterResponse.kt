package kr.dongchimi.api.owner.product.response

import io.swagger.v3.oas.annotations.media.Schema

data class DailyProductRegisterResponse(
    @Schema(description = "등록된 상품 아이디")
    val productId: Long,
)
