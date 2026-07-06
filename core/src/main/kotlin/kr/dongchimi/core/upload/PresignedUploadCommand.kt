package kr.dongchimi.core.upload

data class PresignedUploadCommand(
    val purpose: UploadPurpose,
    val contentType: String,
    val contentLength: Long,
)
