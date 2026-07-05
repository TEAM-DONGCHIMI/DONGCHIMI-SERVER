package kr.dongchimi.gateway.auth.jwt

import kr.dongchimi.core.auth.TokenProvider
import org.springframework.stereotype.Component

@Component
class JwtTokenProvider(
    private val jwtProvider: JwtProvider,
) : TokenProvider {
    override fun issueAccessToken(
        userId: Long,
        roles: Set<String>,
    ): String = jwtProvider.generateToken(userId, roles)
}
