package kr.dongchimi.api.core.common.dto

import kr.dongchimi.core.common.CursorSliceResult

data class CursorSliceResponse<T>(
    val content: List<T>,
    val hasNext: Boolean,
) {
    constructor(cursorSliceResult: CursorSliceResult<T>) : this(cursorSliceResult.content, cursorSliceResult.hasNext)
}
