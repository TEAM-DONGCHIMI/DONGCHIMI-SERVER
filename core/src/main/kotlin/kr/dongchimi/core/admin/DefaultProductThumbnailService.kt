package kr.dongchimi.core.admin

import io.github.oshai.kotlinlogging.KotlinLogging
import kr.dongchimi.core.common.CursorSliceResult
import kr.dongchimi.core.upload.ConfirmedUpload
import kr.dongchimi.core.upload.UploadService
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class DefaultProductThumbnailService(
    private val defaultProductThumbnailReader: DefaultProductThumbnailReader,
    private val defaultProductThumbnailAppender: DefaultProductThumbnailAppender,
    private val defaultProductThumbnailUpdater: DefaultProductThumbnailUpdater,
    private val uploadService: UploadService,
) {
    fun getList(condition: DefaultThumbnailListCondition): CursorSliceResult<DefaultProductThumbnail> =
        defaultProductThumbnailReader.findList(condition)

    fun create(
        items: List<DefaultThumbnailCreateItem>,
        createdBy: Long,
    ): List<DefaultProductThumbnail> {
        val confirmedUploads = mutableListOf<ConfirmedUpload>()
        return try {
            val confirmedItems = items.map { it.copy(thumbnailUrl = confirmIfTempKey(it.thumbnailUrl, confirmedUploads)) }
            defaultProductThumbnailAppender.appendAll(confirmedItems, createdBy)
        } catch (e: Exception) {
            rollback(confirmedUploads)
            throw e
        }
    }

    fun update(command: DefaultThumbnailUpdateCommand) {
        defaultProductThumbnailReader.read(command.id)

        val confirmedUploads = mutableListOf<ConfirmedUpload>()
        try {
            val confirmedUrl = confirmIfTempKey(command.thumbnailUrl, confirmedUploads)
            defaultProductThumbnailUpdater.update(command.copy(thumbnailUrl = confirmedUrl))
        } catch (e: Exception) {
            rollback(confirmedUploads)
            throw e
        }
    }

    private fun confirmIfTempKey(
        thumbnailUrl: String,
        confirmedUploads: MutableList<ConfirmedUpload>,
    ): String {
        val confirmed = uploadService.confirmIfTempKey(thumbnailUrl) ?: return thumbnailUrl
        confirmedUploads.add(confirmed)
        return confirmed.accessUrl
    }

    private fun rollback(confirmedUploads: List<ConfirmedUpload>) {
        confirmedUploads.forEach { confirmed ->
            runCatching { uploadService.deleteObject(confirmed.objectKey) }
                .onFailure { logger.warn(it) { "confirm 롤백 실패, 고아 객체 수동 정리 필요: ${confirmed.objectKey}" } }
        }
    }
}
