package kr.dongchimi.core.common

data class PageOffset(
    val page: Int,
    val size: Int,
) {
    companion object {
        const val DEFAULT_PAGE = 0
        const val DEFAULT_SIZE = 10
    }
}
