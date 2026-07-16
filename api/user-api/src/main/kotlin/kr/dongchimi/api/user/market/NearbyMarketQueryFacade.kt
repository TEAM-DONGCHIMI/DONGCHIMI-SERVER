package kr.dongchimi.api.user.market

import kr.dongchimi.api.core.common.dto.CursorSliceResponse
import kr.dongchimi.api.user.market.response.NearbyMarketResponse
import kr.dongchimi.core.holiday.HolidayService
import kr.dongchimi.core.market.MarketService
import kr.dongchimi.core.market.NearbyMarketSearchCondition
import kr.dongchimi.core.product.ProductService
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class NearbyMarketQueryFacade(
    private val marketService: MarketService,
    private val productService: ProductService,
    private val holidayService: HolidayService,
) {
    fun getNearbyMarkets(
        condition: NearbyMarketSearchCondition,
        now: LocalDateTime,
    ): CursorSliceResponse<NearbyMarketResponse> {
        val slice = marketService.getNearbyMarkets(condition)
        val marketIds = slice.content.map { it.market.id }
        val today = now.toLocalDate()

        val previewProducts =
            productService
                .getLatestActiveProducts(marketIds, today, PREVIEW_PRODUCT_SIZE)
                .groupBy { it.marketId }
        val productCounts = productService.countActiveProductsByMarketIds(marketIds, today)
        val holidays = holidayService.getHolidays(today)

        return CursorSliceResponse(
            content =
                slice.content.map { nearbyMarket ->
                    NearbyMarketResponse(
                        nearbyMarket = nearbyMarket,
                        productCount = productCounts[nearbyMarket.market.id] ?: 0,
                        previewProducts = previewProducts[nearbyMarket.market.id].orEmpty(),
                        now = now,
                        holidays = holidays,
                    )
                },
            hasNext = slice.hasNext,
            nextCursor = slice.nextCursor,
        )
    }

    companion object {
        private const val PREVIEW_PRODUCT_SIZE = 3
    }
}
