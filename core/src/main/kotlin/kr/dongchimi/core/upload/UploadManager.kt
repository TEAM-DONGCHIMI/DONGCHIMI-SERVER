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
        val purpose = objectKeyGenerator.parsePurpose(tempKey)
        val metadata =
            storageClient.getObjectMetadata(tempKey)
                ?: throw CoreException(UploadErrorCode.UPLOAD_NOT_FOUND)

        uploadValidator.validate(purpose, metadata.contentType, metadata.contentLength)

        val permanentKey = objectKeyGenerator.toPermanentKey(tempKey)

        storageClient.moveObject(tempKey, permanentKey)

        return ConfirmedUpload(permanentKey, storageClient.resolveAccessUrl(permanentKey))
    }
}
