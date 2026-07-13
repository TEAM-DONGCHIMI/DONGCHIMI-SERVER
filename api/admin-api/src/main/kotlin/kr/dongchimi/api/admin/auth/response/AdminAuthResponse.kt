package kr.dongchimi.api.admin.auth.response

import io.swagger.v3.oas.annotations.media.Schema

data class AdminAuthResponse(
    @Schema(description = "액세스 토큰 (JWT)")
    val accessToken: String,
)
