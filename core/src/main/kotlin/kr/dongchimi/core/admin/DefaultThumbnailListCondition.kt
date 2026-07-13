package kr.dongchimi.core.admin

data class DefaultThumbnailListCondition(
    val cursor: Long?,
    val search: String?,
    val sort: DefaultThumbnailSortType,
    val size: Int,
)
