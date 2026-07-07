package kr.dongchimi.api.core.auth

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "refresh-token.cookie")
data class RefreshTokenCookieProperties(
    val name: String,
    val path: String,
    val sameSite: String,
    val secure: Boolean,
)
