package kr.dongchimi.api.common.dto

data class PageResponse<T>(
    val content: List<T>,
    val hasNext: Boolean,
)