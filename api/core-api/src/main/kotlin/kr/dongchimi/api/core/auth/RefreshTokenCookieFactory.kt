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
        persistent: Boolean = true,
    ): ResponseCookie {
        val builder =
            ResponseCookie
                .from(properties.name, refreshTokenValue)
                .httpOnly(true)
                .secure(properties.secure)
                .sameSite(properties.sameSite)
                .path(properties.path)

        // persistent=true면 만료시각까지 유지되는 영속 쿠키, false면 maxAge 미설정으로 세션 쿠키(브라우저 종료 시 삭제)
        if (persistent) {
            builder.maxAge(Duration.between(LocalDateTime.now(), expiresAt))
        }

        return builder.build()
    }
}
