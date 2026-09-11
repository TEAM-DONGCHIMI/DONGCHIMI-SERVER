package kr.dongchimi.api.owner.flyer

import kr.dongchimi.api.owner.flyer.response.FlyerDailyPreviewResponse
import kr.dongchimi.api.owner.flyer.response.FlyerPreviewResponse
import kr.dongchimi.core.holiday.HolidayService
import kr.dongchimi.core.market.MarketService
import kr.dongchimi.core.product.DealType
import kr.dongchimi.core.product.PreparedProductService
import kr.dongchimi.core.product.ProductService
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class FlyerPreviewQueryFacade(
    private val marketService: MarketService,
    private val productService: ProductService,
    private val preparedProductService: PreparedProductService,
    private val holidayService: HolidayService,
) {
    fun getPeriodicPreview(
        ownerId: Long,
        marketId: Long,
        now: LocalDateTime,
    ): FlyerPreviewResponse {
        val market = marketService.getByIdForOwner(ownerId, marketId)
        val today = now.toLocalDate()

        val top3 = productService.getPopularActiveProducts(marketId, today, TOP_PRODUCTS_LIMIT)
        val dailyProducts = productService.getAllActiveProducts(marketId, DealType.DAILY, today)
        val preparedProducts = preparedProductService.getPreviewDrafts(ownerId, marketId)
        val holidays = holidayService.getHolidays(today)

        return FlyerPreviewResponse(market, now, holidays, top3, dailyProducts, preparedProducts)
    }

    fun getDailyPreview(
        ownerId: Long,
        marketId: Long,
        now: LocalDateTime,
    ): FlyerDailyPreviewResponse {
        val market = marketService.getByIdForOwner(ownerId, marketId)
        val today = now.toLocalDate()

        val top3 = productService.getPopularActiveProducts(marketId, today, TOP_PRODUCTS_LIMIT)
        val dailyProducts = productService.getAllActiveProducts(marketId, DealType.DAILY, today)
        val holidays = holidayService.getHolidays(today)

        return FlyerDailyPreviewResponse(market, now, holidays, top3, dailyProducts)
    }

    companion object {
        private const val TOP_PRODUCTS_LIMIT = 3
    }
}
