package kr.dongchimi.api.core.dto

import kr.dongchimi.core.common.PageResult

object PageResponseMapper {
    fun <T> PageResult<T>.toPageResponse(): PageResponse<T> {
        return PageResponse(content, hasNext)
    }
}