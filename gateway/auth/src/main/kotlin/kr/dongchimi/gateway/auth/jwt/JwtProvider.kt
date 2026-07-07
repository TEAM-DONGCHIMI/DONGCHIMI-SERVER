package kr.dongchimi.gateway.auth.jwt

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import kr.dongchimi.core.auth.RefreshTokenPayload
import kr.dongchimi.gateway.auth.config.JwtProperties
import kr.dongchimi.gateway.auth.security.UserAuthentication
import org.springframework.stereotype.Component
import java.util.Date
import javax.crypto.SecretKey

@Component
class JwtProvider(
    private val jwtProperties: JwtProperties,
) {
    private val key: SecretKey by lazy {
        Keys.hmacShaKeyFor(jwtProperties.secretKey.toByteArray(Charsets.UTF_8))
    }

    fun generateToken(
        userId: Long,
        roles: Set<String>,
    ): String {
        val now = Date()

        return Jwts
            .builder()
            .subject(userId.toString())
            .issuer(jwtProperties.issuer)
            .issuedAt(now)
            .expiration(Date(now.time + jwtProperties.accessTokenExpiry))
            .claim(CLAIM_ROLES, roles)
            .signWith(key)
            .compact()
    }

    fun generateRefreshToken(
        userId: Long,
        roles: Set<String>,
        tokenId: String,
    ): String {
        val now = Date()

        return Jwts
            .builder()
            .id(tokenId)
            .subject(userId.toString())
            .issuer(jwtProperties.issuer)
            .issuedAt(now)
            .expiration(Date(now.time + jwtProperties.refreshTokenExpiry))
            .claim(CLAIM_ROLES, roles)
            .signWith(key)
            .compact()
    }

    fun parseAuthentication(token: String): UserAuthentication {
        val claims = parseClaims(token)

        val userId = claims.subject.toLong()
        val roles = (claims[CLAIM_ROLES] as List<*>).filterIsInstance<String>().toSet()

        return UserAuthentication(userId, roles)
    }

    fun parseRefreshToken(token: String): RefreshTokenPayload {
        val claims = parseClaims(token)

        val tokenId = claims.id
        val userId = claims.subject.toLong()
        val roles = (claims[CLAIM_ROLES] as List<*>).filterIsInstance<String>().toSet()

        return RefreshTokenPayload(tokenId, userId, roles)
    }

    private fun parseClaims(token: String): Claims =
        Jwts
            .parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload

    companion object {
        private const val CLAIM_ROLES = "roles"
    }
}
