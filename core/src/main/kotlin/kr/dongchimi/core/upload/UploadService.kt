package kr.dongchimi.core.upload

import org.springframework.stereotype.Service

@Service
class UploadService(
    private val uploadManager: UploadManager,
) {
    fun createPresignedUpload(command: PresignedUploadCommand): PresignedUpload =
        uploadManager.issue(command.purpose, command.contentType, command.contentLength)
}
