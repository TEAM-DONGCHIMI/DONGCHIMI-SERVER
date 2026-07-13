package kr.dongchimi.api.owner.auth.request

import io.swagger.v3.oas.annotations.media.Schema
import kr.dongchimi.api.owner.market.request.MarketRegisterRequest
import kr.dongchimi.core.market.MarketRegisterCommand

data class OwnerSignupCompleteRequest(
    @Schema(description = "회원가입 시 발급받은 signupToken")
    val signupToken: String,
    @Schema(description = "등록할 마트 정보")
    val market: MarketRegisterRequest,
) {
    fun toMarketCommand(): MarketRegisterCommand = market.toCommand()
}
