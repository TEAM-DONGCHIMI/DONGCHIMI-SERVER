package kr.dongchimi.api.admin.defaultthumbnail.request

import kr.dongchimi.api.core.common.exception.InvalidInputException
import kr.dongchimi.api.core.common.exception.validate
import kr.dongchimi.core.admin.DefaultThumbnailListCondition
import kr.dongchimi.core.admin.DefaultThumbnailSortType
import kr.dongchimi.core.admin.toDefaultThumbnailSortTypeOrNull
import org.springdoc.core.annotations.ParameterObject

@ParameterObject
data class DefaultThumbnailListRequest(
    val size: Int = DEFAULT_SIZE,
    val cursor: Long? = null,
    val search: String? = null,
    val sort: String? = null,
) {
    fun toCondition(): DefaultThumbnailListCondition {
        validate(size in 1..MAX_SIZE) { "조회 개수는 1 이상 $MAX_SIZE 이하여야 합니다." }
        validate(cursor == null || cursor > 0) { "cursor는 1 이상이어야 합니다." }
        val sortType =
            sort?.let { raw ->
                raw.toDefaultThumbnailSortTypeOrNull() ?: throw InvalidInputException("올바르지 않은 정렬 기준입니다.")
            } ?: DefaultThumbnailSortType.LATEST
        val search = search?.trim()?.takeIf { it.isNotBlank() }?.lowercase()

        return DefaultThumbnailListCondition(cursor = cursor, search = search, sort = sortType, size = size)
    }

    companion object {
        private const val DEFAULT_SIZE = 24
        private const val MAX_SIZE = 60
    }
}
