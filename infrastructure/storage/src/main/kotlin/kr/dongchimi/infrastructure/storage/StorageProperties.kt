package kr.dongchimi.infrastructure.storage

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "storage")
data class StorageProperties(
    val provider: String,
    val presignExpiry: Duration,
    val cdnBaseUrl: String,
)
