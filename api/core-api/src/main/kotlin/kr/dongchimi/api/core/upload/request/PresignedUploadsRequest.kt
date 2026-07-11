package kr.dongchimi.api.core.upload.request

import io.swagger.v3.oas.annotations.media.Schema
import kr.dongchimi.api.core.common.exception.validate
import kr.dongchimi.core.upload.PresignedUploadCommand

data class PresignedUploadsRequest(
    @Schema(description = "발급할 업로드 URL 목록")
    val uploads: List<PresignedUploadRequest>,
) {
    fun toCommands(): List<PresignedUploadCommand> {
        validate(uploads.isNotEmpty()) { "발급할 업로드 정보가 없습니다." }

        return uploads.map { it.toCommand() }
    }
}
