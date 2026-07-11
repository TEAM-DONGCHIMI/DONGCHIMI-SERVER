package kr.dongchimi.api.owner.product.request

import kr.dongchimi.api.core.common.exception.InvalidInputException
import kr.dongchimi.api.core.common.exception.validate
import kr.dongchimi.core.product.ProductKeywordSearchCondition
import org.springdoc.core.annotations.ParameterObject

@ParameterObject
data class ProductSearchRequest(
    val keyword: String? = null,
    val size: Int? = null,
) {
    fun toSearchCondition(): ProductKeywordSearchCondition {
        val keyword =
            keyword?.trim()?.takeIf { it.isNotBlank() }?.lowercase()
                ?: throw InvalidInputException("검색어는 필수입니다.")
        val size = size ?: DEFAULT_SIZE
        validate(size > 0) { "조회 개수는 1 이상이어야 합니다." }
        validate(size <= MAX_SIZE) { "조회 개수는 최대 ${MAX_SIZE}건까지 가능합니다." }

        return ProductKeywordSearchCondition(keyword = keyword, size = size)
    }

    companion object {
        private const val DEFAULT_SIZE = 10
        private const val MAX_SIZE = 50
    }
}
