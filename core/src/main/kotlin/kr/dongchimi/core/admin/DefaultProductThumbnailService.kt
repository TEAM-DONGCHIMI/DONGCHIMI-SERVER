package kr.dongchimi.core.admin

import kr.dongchimi.core.common.CursorSliceResult
import kr.dongchimi.core.upload.ObjectKeyGenerator
import kr.dongchimi.core.upload.UploadService
import org.springframework.stereotype.Service

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
        val confirmedItems = items.map { it.copy(thumbnailUrl = confirmIfTempKey(it.thumbnailUrl)) }
        return defaultProductThumbnailAppender.appendAll(confirmedItems, createdBy)
    }

    fun update(command: DefaultThumbnailUpdateCommand) =
        defaultProductThumbnailUpdater.update(command.copy(thumbnailUrl = confirmIfTempKey(command.thumbnailUrl)))

    private fun confirmIfTempKey(thumbnailUrl: String): String =
        if (thumbnailUrl.startsWith("${ObjectKeyGenerator.TEMP_PREFIX}/")) {
            uploadService.confirmUpload(thumbnailUrl).accessUrl
        } else {
            thumbnailUrl
        }
}
