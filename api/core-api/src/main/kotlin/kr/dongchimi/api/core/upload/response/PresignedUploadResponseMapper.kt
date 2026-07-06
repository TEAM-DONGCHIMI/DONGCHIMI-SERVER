package kr.dongchimi.api.core.upload.response

import kr.dongchimi.core.upload.PresignedUpload

object PresignedUploadResponseMapper {
    fun PresignedUpload.toResponse(): PresignedUploadResponse = PresignedUploadResponse(uploadUrl, objectKey, expiresAt, requiredHeaders)
}
