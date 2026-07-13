package kr.dongchimi.core.admin

import kr.dongchimi.core.common.CursorSliceResult
import org.springframework.stereotype.Service

@Service
class DefaultProductThumbnailService(
    private val defaultProductThumbnailReader: DefaultProductThumbnailReader,
    private val defaultProductThumbnailAppender: DefaultProductThumbnailAppender,
    private val defaultProductThumbnailUpdater: DefaultProductThumbnailUpdater,
) {
    fun getList(condition: DefaultThumbnailListCondition): CursorSliceResult<DefaultProductThumbnail> =
        defaultProductThumbnailReader.findList(condition)

    fun create(
        items: List<DefaultThumbnailCreateItem>,
        createdBy: Long,
    ): List<DefaultProductThumbnail> = defaultProductThumbnailAppender.appendAll(items, createdBy)

    fun update(command: DefaultThumbnailUpdateCommand) = defaultProductThumbnailUpdater.update(command)
}
