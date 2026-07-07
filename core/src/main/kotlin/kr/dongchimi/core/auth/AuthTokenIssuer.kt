package kr.dongchimi.core.auth

import org.springframework.stereotype.Component

@Component
class AuthTokenIssuer(
    private val tokenProvider: TokenProvider,
    private val refreshTokenAppender: RefreshTokenAppender,
) {
    fun issue(
        userId: Long,
        roles: Set<String>,
    ): AuthTokens {
        val accessToken = tokenProvider.issueAccessToken(userId, roles)
        val refreshToken = tokenProvider.issueRefreshToken(userId, roles)

        refreshTokenAppender.append(refreshToken, userId)

        return AuthTokens(
            accessToken = accessToken,
            refreshToken = refreshToken.tokenValue,
            refreshExpiresAt = refreshToken.expiresAt,
        )
    }
}
