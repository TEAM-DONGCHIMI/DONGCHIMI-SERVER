package kr.dongchimi.core.admin

data class DefaultProductThumbnail(
    val id: Long = 0,
    val name: String,
    val thumbnailUrl: String,
    val createdBy: Long,
)
