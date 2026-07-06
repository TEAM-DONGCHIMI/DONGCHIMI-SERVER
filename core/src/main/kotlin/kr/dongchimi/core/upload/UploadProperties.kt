package kr.dongchimi.core.upload

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "upload")
data class UploadProperties(
    val maxSizeBytes: Map<UploadPurpose, Long>,
)
