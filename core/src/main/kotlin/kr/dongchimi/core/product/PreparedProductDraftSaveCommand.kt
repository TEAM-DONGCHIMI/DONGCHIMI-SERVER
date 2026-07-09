package kr.dongchimi.core.product

data class PreparedProductDraftSaveCommand(
    val id: Long,
    val name: String?,
    val thumbnailUrl: String?,
    val price: Price?,
    val category: ProductCategory?,
    val promotionalPhrase: String?,
    val discountPeriod: DiscountPeriod?,
    val dealType: DealType,
)
