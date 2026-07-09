package kr.dongchimi.api.core.common.dto

import kr.dongchimi.api.core.common.exception.validate
import kr.dongchimi.core.common.PageOffset

data class PageOffsetRequest(
    val page: Int? = null,
    val size: Int? = null,
) {
    fun toPageOffset(): PageOffset {
        val page = page ?: PageOffset.DEFAULT_PAGE
        val size = size ?: PageOffset.DEFAULT_SIZE
        validate(page >= 0) { "페이지 번호는 0 이상이어야 합니다." }
        validate(size in 1..MAX_SIZE) { "조회 개수는 1 이상 $MAX_SIZE 이하여야 합니다." }

        return PageOffset(page, size)
    }

    companion object {
        private const val MAX_SIZE = 100
    }
}
