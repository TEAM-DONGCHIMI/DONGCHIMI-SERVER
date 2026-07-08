package kr.dongchimi.api.owner.auth.response

import io.swagger.v3.oas.annotations.media.Schema

data class OwnerSignupResponse(
    @Schema(description = "점주 id")
    val ownerId: Long,
    @Schema(description = "점주 이메일")
    val email: String,
)
