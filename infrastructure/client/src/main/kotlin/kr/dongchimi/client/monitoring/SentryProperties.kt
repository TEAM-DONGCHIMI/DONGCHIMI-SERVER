package kr.dongchimi.client.monitoring

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "monitoring.sentry")
data class SentryProperties(
    val enabled: Boolean = false,
    val dsn: String = "",
    val environment: String = "local",
    val tracesSampleRate: Double = 0.0,
)
