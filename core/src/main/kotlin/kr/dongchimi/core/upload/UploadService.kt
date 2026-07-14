package kr.dongchimi.core.upload

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

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

    fun confirmIfTempKey(url: String): ConfirmedUpload? =
        if (url.startsWith("${ObjectKeyGenerator.TEMP_PREFIX}/")) {
            confirmUpload(url)
        } else {
            null
        }

    /**
     * tmp 키가 섞인 URL을 confirm하면서 도메인 저장 로직(action)을 실행한다.
     * action 도중 예외가 나면 그 사이 permanent로 이동된 객체를 전부 롤백한다.
     */
    fun <T> withConfirmRollback(action: (confirm: (String) -> String) -> T): T {
        val confirmedUploads = mutableListOf<ConfirmedUpload>()

        val confirm: (String) -> String = { url ->
            val confirmed = confirmIfTempKey(url)
            if (confirmed != null) {
                confirmedUploads.add(confirmed)
                confirmed.accessUrl
            } else {
                url
            }
        }

        return try {
            action(confirm)
        } catch (e: Exception) {
            confirmedUploads.forEach { confirmed ->
                runCatching { deleteObject(confirmed.objectKey) }
                    .onFailure { logger.warn(it) { "confirm 롤백 실패, 고아 객체 수동 정리 필요: ${confirmed.objectKey}" } }
            }
            throw e
        }
    }
}
