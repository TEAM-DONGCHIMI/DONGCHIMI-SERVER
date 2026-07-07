package kr.dongchimi.core.auth

import java.time.LocalDateTime

data class RefreshToken(
    val tokenId: String,
    val userId: Long,
    val expiresAt: LocalDateTime,
) {
    constructor(issued: IssuedRefreshToken, userId: Long) : this(issued.tokenId, userId, issued.expiresAt)
}
