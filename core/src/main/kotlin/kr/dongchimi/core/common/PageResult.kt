package kr.dongchimi.core.common

data class PageResult<T>(
    val content: List<T>,
    val hasNext: Boolean,
)
