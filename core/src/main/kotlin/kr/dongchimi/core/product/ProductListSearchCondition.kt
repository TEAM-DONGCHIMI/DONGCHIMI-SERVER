package kr.dongchimi.core.product

data class ProductListSearchCondition(
    val dealType: DealType,
    val category: ProductCategory?,
    val sort: ProductSortType,
    val cursor: Long?,
    val size: Int,
)
