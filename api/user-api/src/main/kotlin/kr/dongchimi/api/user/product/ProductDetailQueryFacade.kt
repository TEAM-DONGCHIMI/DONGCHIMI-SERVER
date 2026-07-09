package kr.dongchimi.api.user.product

import kr.dongchimi.api.user.product.response.ProductDetailResponse
import kr.dongchimi.core.market.MarketService
import kr.dongchimi.core.product.ProductService
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class ProductDetailQueryFacade(
    private val marketService: MarketService,
    private val productService: ProductService,
) {
    @Transactional(readOnly = true)
    fun getDetail(
        marketId: Long,
        productId: Long,
    ): ProductDetailResponse {
        val market = marketService.getById(marketId)
        val product = productService.getDetail(marketId, productId)

        return ProductDetailResponse(product, market.info.name)
    }
}
