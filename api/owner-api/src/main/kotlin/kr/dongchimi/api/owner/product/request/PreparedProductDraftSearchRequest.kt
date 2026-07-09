package kr.dongchimi.api.owner.product.request

import kr.dongchimi.api.core.common.exception.InvalidInputException
import kr.dongchimi.core.product.PreparedProductSearchCondition
import kr.dongchimi.core.product.ProductCategory

data class PreparedProductDraftSearchRequest(
    val search: String? = null,
    val categories: List<String>? = null,
) {
    fun toSearchCondition(): PreparedProductSearchCondition {
        val categories =
            categories.orEmpty().map { raw ->
                ProductCategory.entries.find { it.name == raw }
                    ?: throw InvalidInputException("유효하지 않은 카테고리입니다.")
            }

        return PreparedProductSearchCondition(
            search = search?.trim()?.takeIf { it.isNotBlank() },
            categories = categories,
        )
    }
}
