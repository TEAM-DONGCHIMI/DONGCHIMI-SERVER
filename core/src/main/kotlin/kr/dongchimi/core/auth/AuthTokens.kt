package kr.dongchimi.core.auth

import java.time.LocalDateTime

data class AuthTokens(
    val accessToken: String,
    val refreshToken: String,
    val refreshExpiresAt: LocalDateTime,
)
