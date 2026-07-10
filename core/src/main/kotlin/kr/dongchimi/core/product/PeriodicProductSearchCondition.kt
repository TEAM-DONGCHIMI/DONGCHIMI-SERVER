package kr.dongchimi.core.product

data class PeriodicProductSearchCondition(
    val category: ProductCategory?,
    val cursor: Long?,
    val size: Int,
)
