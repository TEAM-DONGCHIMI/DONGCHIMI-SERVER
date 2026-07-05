package kr.dongchimi.core.product

data class PreparedProduct(
    val id: Long = 0,
    val marketId: Long,
    val name: String?,
    val thumbnailUrl: String?,
    val price: Price?,
    val category: ProductCategory?,
    val promotionalPhrase: String?,
    val discountPeriod: DiscountPeriod?,
    val draftStatus: DraftStatus,
    val failReason: String?,
)
