package kr.dongchimi.core.common

data class CursorSliceResult<T>(
    val content: List<T>,
    val hasNext: Boolean,
    // 다음 페이지 조회용 커서. 마지막 페이지면 null이다.
    val nextCursor: Long? = null,
)
