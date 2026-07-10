package kr.dongchimi.core.product

data class DailyDealRegisterCommand(
    val name: String,
    val thumbnailUrl: String?,
    val price: Price,
    val category: ProductCategory,
    val promotionalPhrase: String?,
    val discountPeriod: DiscountPeriod,
) {
    fun toProduct(marketId: Long): Product =
        Product(
            marketId = marketId,
            name = name,
            dealType = DealType.DAILY,
            thumbnailUrl = thumbnailUrl,
            price = price,
            category = category,
            promotionalPhrase = promotionalPhrase,
            discountPeriod = discountPeriod,
        )
}
