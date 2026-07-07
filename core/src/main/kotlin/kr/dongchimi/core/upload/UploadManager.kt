package kr.dongchimi.core.upload

import kr.dongchimi.core.common.exception.CoreException
import org.springframework.stereotype.Component

@Component
class UploadManager(
    private val storageClient: StorageClient,
    private val objectKeyGenerator: ObjectKeyGenerator,
    private val uploadValidator: UploadValidator,
) {
    fun issuePresignedUpload(
        purpose: UploadPurpose,
        contentType: String,
        contentLength: Long,
    ): PresignedUpload {
        uploadValidator.validate(purpose, contentType, contentLength)

        val tempKey = objectKeyGenerator.generateTempKey(purpose, contentType)

        return storageClient.createUploadUrl(tempKey, contentType, contentLength)
    }

    fun confirmUpload(tempKey: String): ConfirmedUpload {
        val permanentKey = objectKeyGenerator.toPermanentKey(tempKey)
        val metadata = storageClient.getObjectMetadata(tempKey)

        if (metadata == null) {
            storageClient
                .getObjectMetadata(permanentKey)
                ?.let { return ConfirmedUpload(permanentKey, storageClient.resolveAccessUrl(permanentKey)) }
            throw CoreException(UploadErrorCode.UPLOAD_NOT_FOUND)
        }

        val purpose = objectKeyGenerator.parsePurpose(tempKey)
        uploadValidator.validate(purpose, metadata.contentType, metadata.contentLength)

        storageClient.moveObject(tempKey, permanentKey)

        return ConfirmedUpload(permanentKey, storageClient.resolveAccessUrl(permanentKey))
    }
}
