package kr.dongchimi.client.monitoring

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "monitoring.discord")
data class DiscordProperties(
    val enabled: Boolean = false,
    val webhookUrl: String = "",
    val connectTimeout: Duration = Duration.ofSeconds(2),
    val readTimeout: Duration = Duration.ofSeconds(3),
)
