package kr.dongchimi.core.admin

enum class DefaultThumbnailSortType { LATEST, NAME }

fun String.toDefaultThumbnailSortTypeOrNull(): DefaultThumbnailSortType? = DefaultThumbnailSortType.entries.find { it.name == this }
