package kr.dongchimi.gateway.auth.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "jwt")
data class JwtProperties(
    val secretKey: String,
    val issuer: String,
    val accessTokenExpiry: Long,
    val refreshTokenExpiry: Long,
)
