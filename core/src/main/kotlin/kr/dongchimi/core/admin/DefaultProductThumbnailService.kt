package kr.dongchimi.core.admin

import kr.dongchimi.core.common.CursorSliceResult
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
    ): List<DefaultProductThumbnail> =
        uploadService.withConfirmRollback { confirm ->
            val confirmedItems = items.map { it.copy(thumbnailUrl = confirm(it.thumbnailUrl)) }
            defaultProductThumbnailAppender.appendAll(confirmedItems, createdBy)
        }

    fun update(command: DefaultThumbnailUpdateCommand) {
        defaultProductThumbnailReader.read(command.id)

        uploadService.withConfirmRollback { confirm ->
            defaultProductThumbnailUpdater.update(command.copy(thumbnailUrl = confirm(command.thumbnailUrl)))
        }
    }
}
