package kr.dongchimi.api.core.common.dto

data class PageResponse<T>(
    val content: List<T>,
    val hasNext: Boolean,
)
