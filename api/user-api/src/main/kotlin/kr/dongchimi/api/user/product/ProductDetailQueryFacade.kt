package kr.dongchimi.api.user.product

import kr.dongchimi.api.user.product.response.ProductDetailResponse
import kr.dongchimi.core.market.MarketService
import kr.dongchimi.core.product.ProductService
import kr.dongchimi.core.viewcount.EntityViewedEvent
import kr.dongchimi.core.viewcount.ViewTarget
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class ProductDetailQueryFacade(
    private val marketService: MarketService,
    private val productService: ProductService,
    private val eventPublisher: ApplicationEventPublisher,
) {
    @Transactional(readOnly = true)
    fun getDetail(
        marketId: Long,
        productId: Long,
        userId: Long,
    ): ProductDetailResponse {
        val market = marketService.getById(marketId)
        val product = productService.getDetail(marketId, productId)

        eventPublisher.publishEvent(EntityViewedEvent(ViewTarget.PRODUCT, productId, userId))

        return ProductDetailResponse(product, market.info.name)
    }
}
