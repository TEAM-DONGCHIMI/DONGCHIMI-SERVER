package kr.dongchimi.core.product

data class PreparedProductSearchCondition(
    val search: String?,
    val categories: List<ProductCategory> = emptyList(),
)
