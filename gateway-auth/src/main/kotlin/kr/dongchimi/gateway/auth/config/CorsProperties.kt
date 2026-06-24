package kr.dongchimi.gateway.auth.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "cors")
data class CorsProperties(
    val allowedOrigins: List<String>,
    val allowedMethods: List<String>,
    val allowedHeaders: List<String>,
)