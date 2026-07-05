package kr.dongchimi.core.product

data class Product(
    val id: Long = 0,
    val marketId: Long,
    val name: String,
    val dealType: DealType,
    val thumbnailUrl: String,
    val price: Price,
    val category: ProductCategory,
    val promotionalPhrase: String?,
    val discountPeriod: DiscountPeriod,
)
