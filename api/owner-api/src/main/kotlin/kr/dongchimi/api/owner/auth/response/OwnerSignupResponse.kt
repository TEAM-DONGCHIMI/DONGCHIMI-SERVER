package kr.dongchimi.api.owner.auth.response

import io.swagger.v3.oas.annotations.media.Schema

data class OwnerSignupResponse(
    @Schema(description = "회원가입 완료(마트 등록)에 사용할 임시 토큰")
    val signupToken: String,
    @Schema(description = "점주 이메일")
    val email: String,
)
