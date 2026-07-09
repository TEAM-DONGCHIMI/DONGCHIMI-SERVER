package kr.dongchimi.core.common

data class PageResult<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalCount: Long,
    val totalPage: Int,
)
