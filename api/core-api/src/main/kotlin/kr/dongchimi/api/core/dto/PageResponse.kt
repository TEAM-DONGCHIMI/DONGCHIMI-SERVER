package kr.dongchimi.api.core.dto

data class PageResponse<T>(
    val content: List<T>,
    val hasNext: Boolean,
)
