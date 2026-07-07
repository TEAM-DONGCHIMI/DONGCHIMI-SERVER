package kr.dongchimi.core.auth

data class RefreshTokenPayload(
    val tokenId: String,
    val userId: Long,
    val roles: Set<String>,
)
