package kr.dongchimi.api.core.upload.response

import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant

data class PresignedUploadResponse(
    @Schema(description = "S3에 직접 PUT할 업로드 URL")
    val uploadUrl: String,
    @Schema(description = "임시 저장 경로(tmp key). 리소스 확정 요청 시 그대로 되돌려줘야 한다")
    val objectKey: String,
    @Schema(description = "업로드 URL 만료 시각")
    val expiresAt: Instant,
    @Schema(description = "PUT 요청 시 그대로 실어야 하는 헤더")
    val requiredHeaders: Map<String, String>,
)
