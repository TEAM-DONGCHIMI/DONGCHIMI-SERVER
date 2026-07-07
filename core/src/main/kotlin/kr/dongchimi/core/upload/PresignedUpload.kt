package kr.dongchimi.core.upload

import java.time.LocalDateTime

data class PresignedUpload(
    val uploadUrl: String,
    val objectKey: String,
    val expiresAt: LocalDateTime,
    val requiredHeaders: Map<String, String> = emptyMap(),
)
