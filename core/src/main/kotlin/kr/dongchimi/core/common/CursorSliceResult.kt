package kr.dongchimi.core.common

data class CursorSliceResult<T>(
    val content: List<T>,
    val hasNext: Boolean,
)
