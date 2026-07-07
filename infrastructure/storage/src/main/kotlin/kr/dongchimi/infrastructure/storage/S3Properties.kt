package kr.dongchimi.infrastructure.storage

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "storage.s3")
data class S3Properties(
    val region: String,
    val bucket: String,
    val endpoint: String? = null,
    val pathStyleAccess: Boolean = false,
)
