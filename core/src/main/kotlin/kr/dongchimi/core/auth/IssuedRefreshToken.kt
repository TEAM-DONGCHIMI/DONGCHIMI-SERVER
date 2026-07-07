package kr.dongchimi.core.auth

import java.time.LocalDateTime

data class IssuedRefreshToken(
    val tokenValue: String,
    val tokenId: String,
    val expiresAt: LocalDateTime,
)
