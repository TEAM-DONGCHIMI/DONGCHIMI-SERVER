package kr.dongchimi.core.upload

import java.time.Instant

data class PresignedUpload(
    val uploadUrl: String,
    val objectKey: String,
    val expiresAt: Instant,
    val requiredHeaders: Map<String, String> = emptyMap(),
)
