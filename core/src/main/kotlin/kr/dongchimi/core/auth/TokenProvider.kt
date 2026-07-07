package kr.dongchimi.core.auth

interface TokenProvider {
    fun issueAccessToken(
        userId: Long,
        roles: Set<String>,
    ): String

    fun issueRefreshToken(
        userId: Long,
        roles: Set<String>,
    ): IssuedRefreshToken

    fun parseRefreshToken(tokenValue: String): RefreshTokenPayload
}
