package kr.dongchimi.core.auth

import org.springframework.stereotype.Service

@Service
class ReissueTokenService(
    private val tokenProvider: TokenProvider,
    private val refreshTokenRotator: RefreshTokenRotator,
) {
    fun reissue(refreshTokenValue: String): AuthTokens {
        val payload = tokenProvider.parseRefreshToken(refreshTokenValue)

        val issuedRefreshToken = tokenProvider.issueRefreshToken(payload.userId, payload.roles)

        refreshTokenRotator.rotate(
            oldTokenId = payload.tokenId,
            issued = issuedRefreshToken,
            userId = payload.userId,
        )

        val accessToken = tokenProvider.issueAccessToken(payload.userId, payload.roles)

        return AuthTokens(
            accessToken = accessToken,
            refreshToken = issuedRefreshToken.tokenValue,
            refreshExpiresAt = issuedRefreshToken.expiresAt,
        )
    }
}
