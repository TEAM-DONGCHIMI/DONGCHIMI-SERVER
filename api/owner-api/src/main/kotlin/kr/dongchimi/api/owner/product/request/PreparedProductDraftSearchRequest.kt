package kr.dongchimi.api.owner.product.request

import kr.dongchimi.api.core.common.exception.InvalidInputException
import kr.dongchimi.core.product.PreparedProductSearchCondition
import kr.dongchimi.core.product.toProductCategoryOrNull
import org.springdoc.core.annotations.ParameterObject

@ParameterObject
data class PreparedProductDraftSearchRequest(
    val search: String? = null,
    val categories: List<String>? = null,
) {
    fun toSearchCondition(): PreparedProductSearchCondition {
        val categories =
            categories.orEmpty().map { raw ->
                raw.toProductCategoryOrNull() ?: throw InvalidInputException("유효하지 않은 카테고리입니다.")
            }

        return PreparedProductSearchCondition(
            search = search?.trim()?.takeIf { it.isNotBlank() },
            categories = categories,
        )
    }
}
