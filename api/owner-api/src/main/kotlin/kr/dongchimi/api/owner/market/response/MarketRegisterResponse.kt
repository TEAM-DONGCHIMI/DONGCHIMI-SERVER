package kr.dongchimi.api.owner.market.response

import io.swagger.v3.oas.annotations.media.Schema

data class MarketRegisterResponse(
    @Schema(description = "등록된 마트 아이디")
    val marketId: Long,
)
