package kr.dongchimi.api.owner.auth.response

import io.swagger.v3.oas.annotations.media.Schema

data class OwnerSignupResponse(
    @Schema(description = "사장님 id")
    val ownerId: Long,
    @Schema(description = "사장님 이메일")
    val email: String,
)
