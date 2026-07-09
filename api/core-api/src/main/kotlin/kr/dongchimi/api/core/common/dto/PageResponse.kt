package kr.dongchimi.api.core.common.dto

import kr.dongchimi.core.common.PageResult

data class PageResponse<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalCount: Long,
    val totalPage: Int,
) {
    constructor(pageResult: PageResult<T>) : this(
        content = pageResult.content,
        page = pageResult.page,
        size = pageResult.size,
        totalCount = pageResult.totalCount,
        totalPage = pageResult.totalPage,
    )
}
