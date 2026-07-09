package kr.dongchimi.core.common

data class CursorSliceResult<T>(
    val content: List<T>,
    val hasNext: Boolean,
    // 다음 페이지 조회용 커서. 마지막 페이지면 null이다.
    val nextCursor: Long? = null,
)

// 호출 측이 size + 1개를 미리 조회해서 넘겨준다는 전제하에 hasNext/nextCursor를 계산한다.
fun <T> List<T>.toCursorSlice(
    size: Int,
    cursorOf: (T) -> Long,
): CursorSliceResult<T> {
    val content = take(size)
    val hasNext = this.size > size

    return CursorSliceResult(
        content = content,
        hasNext = hasNext,
        nextCursor = if (hasNext) cursorOf(content.last()) else null,
    )
}
