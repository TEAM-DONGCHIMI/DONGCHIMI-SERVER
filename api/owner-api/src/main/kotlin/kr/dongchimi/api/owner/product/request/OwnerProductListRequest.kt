package kr.dongchimi.api.owner.product.request

import kr.dongchimi.api.core.common.exception.InvalidInputException
import kr.dongchimi.api.core.common.exception.validate
import kr.dongchimi.core.product.DealType
import kr.dongchimi.core.product.ProductListSearchCondition
import kr.dongchimi.core.product.ProductSortType
import kr.dongchimi.core.product.toProductCategoryOrNull
import kr.dongchimi.core.product.toProductSortTypeOrNull
import org.springdoc.core.annotations.ParameterObject

@ParameterObject
data class OwnerProductListRequest(
    val type: String? = null,
    val sort: String? = null,
    val category: String? = null,
    val cursor: Long? = null,
    val size: Int? = null,
) {
    fun toSearchCondition(): ProductListSearchCondition {
        val dealType =
            type?.let { raw ->
                DealType.entries.find { it.name == raw } ?: throw InvalidInputException("판매 유형이 올바르지 않습니다.")
            } ?: throw InvalidInputException("판매 유형은 필수입니다.")
        val sortType =
            sort?.let { raw ->
                raw.toProductSortTypeOrNull() ?: throw InvalidInputException("유효하지 않은 정렬 기준입니다.")
            } ?: ProductSortType.CATEGORY
        val category =
            category?.takeIf { it.isNotBlank() }?.let { raw ->
                raw.toProductCategoryOrNull() ?: throw InvalidInputException("카테고리가 올바르지 않습니다.")
            }
        val size = size ?: DEFAULT_SIZE
        validate(cursor == null || cursor > 0) { "cursor는 1 이상이어야 합니다." }
        validate(size in 1..MAX_SIZE) { "조회 개수는 1 이상 ${MAX_SIZE}개 이하여야 합니다." }

        return ProductListSearchCondition(
            dealType = dealType,
            category = category,
            sort = sortType,
            cursor = cursor,
            size = size,
        )
    }

    companion object {
        private const val DEFAULT_SIZE = 12
        private const val MAX_SIZE = 60
    }
}
