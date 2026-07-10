package kr.dongchimi.core.product

data class ProductUpdateCommand(
    val dealType: DealType,
    val name: String,
    val thumbnailUrl: String?,
    val price: Price,
    val category: ProductCategory,
    val promotionalPhrase: String?,
    val discountPeriod: DiscountPeriod,
) {
    /** 기존 상품에 수정 필드를 적용한다. id·marketId·dealType은 유지. */
    fun applyTo(product: Product): Product =
        product.copy(
            name = name,
            thumbnailUrl = thumbnailUrl,
            price = price,
            category = category,
            promotionalPhrase = promotionalPhrase,
            discountPeriod = discountPeriod,
        )
}
