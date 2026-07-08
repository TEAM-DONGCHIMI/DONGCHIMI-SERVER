package kr.dongchimi.api.core.common.dto

import kr.dongchimi.core.common.PageResult

data class PageResponse<T>(
    val content: List<T>,
    val hasNext: Boolean,
) {
    constructor(pageResult: PageResult<T>) : this(pageResult.content, pageResult.hasNext)
}
