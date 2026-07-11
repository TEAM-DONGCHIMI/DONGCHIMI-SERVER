package kr.dongchimi.core.admin

import kr.dongchimi.core.common.CursorSliceResult
import org.springframework.stereotype.Service

@Service
class DefaultProductThumbnailService(
    private val defaultProductThumbnailReader: DefaultProductThumbnailReader,
    private val defaultProductThumbnailAppender: DefaultProductThumbnailAppender,
) {
    fun getList(condition: DefaultThumbnailListCondition): CursorSliceResult<DefaultProductThumbnail> =
        defaultProductThumbnailReader.findList(condition)

    fun bulkCreate(
        command: DefaultThumbnailBulkCreateCommand,
        createdBy: Long,
    ): List<DefaultProductThumbnail> = defaultProductThumbnailAppender.appendAll(command, createdBy)
}
