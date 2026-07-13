package kr.dongchimi.core.upload

import org.springframework.stereotype.Service

@Service
class UploadService(
    private val uploadManager: UploadManager,
) {
    fun createPresignedUpload(command: PresignedUploadCommand): PresignedUpload =
        uploadManager.issuePresignedUpload(command.purpose, command.contentType, command.contentLength)

    fun createPresignedUploads(commands: List<PresignedUploadCommand>): List<PresignedUpload> =
        commands.map { uploadManager.issuePresignedUpload(it.purpose, it.contentType, it.contentLength) }

    fun confirmUpload(tempKey: String): ConfirmedUpload = uploadManager.confirmUpload(tempKey)

    fun deleteObject(objectKey: String) = uploadManager.deleteObject(objectKey)
}
