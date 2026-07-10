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
    val dealType: DealType = DealType.PERIODIC,
    val draftStatus: DraftStatus,
    val failReason: DraftFailReason?,
) {
    /**
     * [PreparedProductValidator.validateAllCompleted]로 [draftStatus]가 SUCCESS임을 확인한 뒤 호출한다.
     */
    fun toProduct(): Product =
        Product(
            marketId = marketId,
            name = name!!,
            dealType = dealType,
            thumbnailUrl = thumbnailUrl,
            price = price!!,
            category = category!!,
            promotionalPhrase = promotionalPhrase,
            discountPeriod = discountPeriod!!,
        )
}
