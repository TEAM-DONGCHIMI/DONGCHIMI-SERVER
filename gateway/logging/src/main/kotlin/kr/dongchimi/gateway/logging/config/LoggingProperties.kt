package kr.dongchimi.gateway.logging.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "gateway.logging")
data class LoggingProperties(
    val excludePaths: List<String> = emptyList(),
)
