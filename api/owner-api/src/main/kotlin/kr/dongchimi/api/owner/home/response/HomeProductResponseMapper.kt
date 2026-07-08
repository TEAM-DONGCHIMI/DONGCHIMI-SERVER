package kr.dongchimi.api.owner.home.response

import kr.dongchimi.core.product.Product

object HomeProductResponseMapper {
    fun Product.toHomeProductResponse() =
        HomeProductResponse(
            productId = id,
            thumbnailUrl = thumbnailUrl,
            name = name,
            originalPrice = price.originalPrice,
            discountedPrice = price.discountedPrice,
            discountRate = price.discountRate(),
        )
}
