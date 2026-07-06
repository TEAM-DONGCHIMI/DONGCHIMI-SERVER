package kr.dongchimi.api.user.auth

import io.swagger.v3.oas.annotations.media.Schema

data class OAuthLoginResponse(
    @Schema(description = "동치미 서비스 access token (JWT)")
    val accessToken: String,
)
