package kr.dongchimi.api.core.auth.response

import io.swagger.v3.oas.annotations.media.Schema

data class ReissueTokenResponse(
    @Schema(description = "동치미 서비스 access token (JWT)")
    val accessToken: String,
)
