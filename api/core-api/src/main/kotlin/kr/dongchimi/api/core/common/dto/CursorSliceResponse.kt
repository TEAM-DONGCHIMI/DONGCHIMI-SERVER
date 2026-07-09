package kr.dongchimi.api.core.common.dto

import io.swagger.v3.oas.annotations.media.Schema
import kr.dongchimi.core.common.CursorSliceResult

data class CursorSliceResponse<T>(
    @Schema(description = "조회 결과 목록")
    val content: List<T>,
    @Schema(description = "다음 페이지 존재 여부")
    val hasNext: Boolean,
    @Schema(description = "다음 페이지 조회용 커서. 없으면 null")
    val nextCursor: Long? = null,
) {
    constructor(cursorSliceResult: CursorSliceResult<T>) : this(
        cursorSliceResult.content,
        cursorSliceResult.hasNext,
        cursorSliceResult.nextCursor,
    )
}
