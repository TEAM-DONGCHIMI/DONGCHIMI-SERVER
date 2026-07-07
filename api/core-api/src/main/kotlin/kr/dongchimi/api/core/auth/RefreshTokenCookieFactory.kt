package kr.dongchimi.api.core.auth

import org.springframework.http.ResponseCookie
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.LocalDateTime

@Component
class RefreshTokenCookieFactory(
    private val properties: RefreshTokenCookieProperties,
) {
    fun create(
        refreshTokenValue: String,
        expiresAt: LocalDateTime,
    ): ResponseCookie =
        ResponseCookie
            .from(properties.name, refreshTokenValue)
            .httpOnly(true)
            .secure(properties.secure)
            .sameSite(properties.sameSite)
            .path(properties.path)
            .maxAge(Duration.between(LocalDateTime.now(), expiresAt))
            .build()
}
