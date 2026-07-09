package kr.dongchimi.api.user.product.request

import kr.dongchimi.api.core.common.exception.InvalidInputException
import kr.dongchimi.api.core.common.exception.validate
import kr.dongchimi.core.product.PeriodicProductSearchCondition
import kr.dongchimi.core.product.ProductCategory
import org.springdoc.core.annotations.ParameterObject

@ParameterObject
data class PeriodicProductListRequest(
    val category: String? = null,
    val cursor: Long? = null,
    val size: Int? = null,
) {
    fun toSearchCondition(): PeriodicProductSearchCondition {
        val category =
            category?.let { raw ->
                ProductCategory.entries.find { it.name == raw }
                    ?: throw InvalidInputException("카테고리가 올바르지 않습니다.")
            }
        val size = size ?: DEFAULT_SIZE
        validate(cursor == null || cursor > 0) { "cursor는 1 이상이어야 합니다." }
        validate(size in 1..MAX_SIZE) { "조회 개수는 1 이상 ${MAX_SIZE}개 이하여야 합니다." }

        return PeriodicProductSearchCondition(
            category = category,
            cursor = cursor,
            size = size,
        )
    }

    companion object {
        private const val DEFAULT_SIZE = 12
        private const val MAX_SIZE = 60
    }
}
