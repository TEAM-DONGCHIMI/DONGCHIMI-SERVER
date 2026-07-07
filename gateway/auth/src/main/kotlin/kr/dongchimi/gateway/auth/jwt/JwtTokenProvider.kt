package kr.dongchimi.gateway.auth.jwt

import kr.dongchimi.core.auth.AuthErrorCode
import kr.dongchimi.core.auth.IssuedRefreshToken
import kr.dongchimi.core.auth.RefreshTokenPayload
import kr.dongchimi.core.auth.TokenProvider
import kr.dongchimi.core.common.exception.CoreException
import kr.dongchimi.gateway.auth.config.JwtProperties
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.LocalDateTime
import java.util.UUID

@Component
class JwtTokenProvider(
    private val jwtProvider: JwtProvider,
    private val jwtProperties: JwtProperties,
) : TokenProvider {
    override fun issueAccessToken(
        userId: Long,
        roles: Set<String>,
    ): String = jwtProvider.generateToken(userId, roles)

    override fun issueRefreshToken(
        userId: Long,
        roles: Set<String>,
    ): IssuedRefreshToken {
        val tokenId = UUID.randomUUID().toString()
        val tokenValue = jwtProvider.generateRefreshToken(userId, roles, tokenId)
        val expiresAt = LocalDateTime.now().plus(Duration.ofMillis(jwtProperties.refreshTokenExpiry))

        return IssuedRefreshToken(tokenValue, tokenId, expiresAt)
    }

    override fun parseRefreshToken(tokenValue: String): RefreshTokenPayload =
        runCatching { jwtProvider.parseRefreshToken(tokenValue) }
            .getOrElse { throw CoreException(AuthErrorCode.INVALID_REFRESH_TOKEN) }
}
