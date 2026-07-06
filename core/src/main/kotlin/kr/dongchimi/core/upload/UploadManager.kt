package kr.dongchimi.core.upload

import kr.dongchimi.core.common.exception.CoreException
import org.springframework.stereotype.Component

@Component
class UploadManager(
    private val storageClient: StorageClient,
    private val objectKeyGenerator: ObjectKeyGenerator,
    private val contentTypeValidator: UploadContentTypeValidator,
    private val uploadProperties: UploadProperties,
) {
    fun issuePresignedUpload(
        purpose: UploadPurpose,
        contentType: String,
        contentLength: Long,
    ): PresignedUpload {
        contentTypeValidator.validate(purpose, contentType)
        if (contentLength > uploadProperties.maxSizeBytes.getValue(purpose)) {
            throw CoreException(UploadErrorCode.FILE_TOO_LARGE)
        }
        val tempKey = objectKeyGenerator.generateTempKey(purpose, contentType)
        return storageClient.createUploadUrl(tempKey, contentType, contentLength)
    }

    fun promoteUpload(tempKey: String): PromotedUpload {
        val purpose = objectKeyGenerator.parsePurpose(tempKey)
        val metadata =
            storageClient.getObjectMetadata(tempKey)
                ?: throw CoreException(UploadErrorCode.UPLOAD_NOT_FOUND)
        contentTypeValidator.validate(purpose, metadata.contentType)
        if (metadata.contentLength > uploadProperties.maxSizeBytes.getValue(purpose)) {
            throw CoreException(UploadErrorCode.FILE_TOO_LARGE)
        }
        val permanentKey = objectKeyGenerator.toPermanentKey(tempKey)
        storageClient.moveObject(tempKey, permanentKey)
        return PromotedUpload(permanentKey, storageClient.resolveAccessUrl(permanentKey))
    }
}
