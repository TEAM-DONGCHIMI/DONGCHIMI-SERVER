package kr.dongchimi.core.admin

import kr.dongchimi.core.common.CursorSliceResult
import kr.dongchimi.core.common.toCursorSlice
import org.springframework.stereotype.Component

@Component
class DefaultProductThumbnailReader(
    private val defaultProductThumbnailRepository: DefaultProductThumbnailRepository,
) {
    fun findList(condition: DefaultThumbnailListCondition): CursorSliceResult<DefaultProductThumbnail> {
        val rows =
            when (condition.sort) {
                DefaultThumbnailSortType.LATEST ->
                    defaultProductThumbnailRepository.findAllByLatest(condition.search, condition.cursor, condition.size + 1)
                DefaultThumbnailSortType.NAME -> {
                    val cursorName = condition.cursor?.let { defaultProductThumbnailRepository.findNameById(it) }
                    if (condition.cursor != null && cursorName == null) {
                        emptyList()
                    } else {
                        defaultProductThumbnailRepository.findAllByName(
                            condition.search,
                            condition.cursor,
                            cursorName,
                            condition.size + 1,
                        )
                    }
                }
            }
        return rows.toCursorSlice(condition.size) { it.id }
    }
}
