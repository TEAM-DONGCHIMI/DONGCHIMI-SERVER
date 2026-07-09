package kr.dongchimi.api.user.market

import kr.dongchimi.api.user.market.response.BusinessHourResponse
import kr.dongchimi.api.user.market.response.MarketDetailResponse
import kr.dongchimi.api.user.market.response.PopularProductResponse
import kr.dongchimi.core.market.MarketService
import kr.dongchimi.core.product.ProductService
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Component
class MarketDetailQueryFacade(
    private val marketService: MarketService,
    private val productService: ProductService,
) {
    @Transactional(readOnly = true)
    fun getDetail(
        slug: String,
        now: LocalDateTime,
    ): MarketDetailResponse {
        val market = marketService.getBySlug(slug)
        val top3 = productService.getPopularActiveProducts(market.id, now.toLocalDate(), TOP_PRODUCTS_LIMIT)

        return MarketDetailResponse(
            marketId = market.id,
            name = market.info.name,
            thumbnailUrl = market.info.thumbnailUrl,
            address = market.info.address.substringBefore("|"),
            isOpenNow = market.businessHours.isOpenAt(now),
            businessHours = market.businessHours.slots.map { BusinessHourResponse(it) },
            marketPhone1 = market.phoneNumber.marketPhone1,
            marketPhone2 = market.phoneNumber.marketPhone2,
            ownerPhone = market.phoneNumber.ownerPhone,
            top3 = top3.map { PopularProductResponse(it) },
        )
    }

    companion object {
        private const val TOP_PRODUCTS_LIMIT = 3
    }
}
