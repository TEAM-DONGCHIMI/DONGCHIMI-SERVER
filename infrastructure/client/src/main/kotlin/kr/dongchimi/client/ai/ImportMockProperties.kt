package kr.dongchimi.client.ai

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "import.mock")
data class ImportMockProperties(
    val latency: Duration,
)
