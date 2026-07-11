package kr.dongchimi.core.admin

import kr.dongchimi.core.common.CursorSliceResult
import org.springframework.stereotype.Service

@Service
class DefaultProductThumbnailService(
    private val defaultProductThumbnailReader: DefaultProductThumbnailReader,
) {
    fun getList(condition: DefaultThumbnailListCondition): CursorSliceResult<DefaultProductThumbnail> =
        defaultProductThumbnailReader.findList(condition)
}
