package kr.dongchimi.api.core.upload.response

import io.swagger.v3.oas.annotations.media.Schema

data class PresignedUploadsResponse(
    @Schema(description = "발급된 업로드 URL 목록")
    val uploads: List<PresignedUploadResponse>,
)
