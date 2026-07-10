package kr.dongchimi.core.product

enum class ProductSortType {
    CATEGORY,
    LATEST,
    VIEW_COUNT,
}

fun String.toProductSortTypeOrNull(): ProductSortType? = ProductSortType.entries.find { it.name == this }
