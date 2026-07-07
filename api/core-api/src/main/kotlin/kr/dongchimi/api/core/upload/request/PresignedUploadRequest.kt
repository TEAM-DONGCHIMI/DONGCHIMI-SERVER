package kr.dongchimi.api.core.upload.request

import io.swagger.v3.oas.annotations.media.Schema
import kr.dongchimi.api.core.common.exception.validate
import kr.dongchimi.core.common.exception.CoreException
import kr.dongchimi.core.upload.PresignedUploadCommand
import kr.dongchimi.core.upload.UploadErrorCode
import kr.dongchimi.core.upload.UploadPurpose

data class PresignedUploadRequest(
    @Schema(description = "업로드 용도", example = "product_thumbnail")
    val purpose: String,
    @Schema(description = "업로드할 파일의 Content-Type", example = "image/jpeg")
    val contentType: String,
    @Schema(description = "업로드할 파일 크기(바이트)", example = "1048576")
    val contentLength: Long,
) {
    fun toCommand(): PresignedUploadCommand {
        validate(contentType.isNotBlank()) { "Content-Type은 필수로 입력해 주세요." }
        validate(contentLength > 0) { "파일 크기는 0보다 커야 합니다." }

        val uploadPurpose =
            runCatching { UploadPurpose.valueOf(purpose.uppercase()) }
                .getOrElse { throw CoreException(UploadErrorCode.UNSUPPORTED_UPLOAD_PURPOSE) }

        return PresignedUploadCommand(uploadPurpose, contentType, contentLength)
    }
}
