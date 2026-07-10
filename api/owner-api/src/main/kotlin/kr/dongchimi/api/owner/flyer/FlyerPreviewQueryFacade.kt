package kr.dongchimi.api.owner.flyer

import kr.dongchimi.api.owner.flyer.response.FlyerDailyPreviewResponse
import kr.dongchimi.api.owner.flyer.response.FlyerPreviewBusinessHourResponse
import kr.dongchimi.api.owner.flyer.response.FlyerPreviewDailyResponse
import kr.dongchimi.api.owner.flyer.response.FlyerPreviewPreparedProductResponse
import kr.dongchimi.api.owner.flyer.response.FlyerPreviewProductResponse
import kr.dongchimi.api.owner.flyer.response.FlyerPreviewResponse
import kr.dongchimi.core.market.MarketService
import kr.dongchimi.core.product.DealType
import kr.dongchimi.core.product.PreparedProductService
import kr.dongchimi.core.product.ProductService
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Component
class FlyerPreviewQueryFacade(
    private val marketService: MarketService,
    private val productService: ProductService,
    private val preparedProductService: PreparedProductService,
) {
    @Transactional(readOnly = true)
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

        return FlyerPreviewResponse(
            marketId = market.id,
            name = market.info.name,
            thumbnailUrl = market.info.thumbnailUrl,
            address = market.info.address.substringBefore("|"),
            isOpenNow = market.businessHours.isOpenAt(now),
            businessHours = market.businessHours.slots.map { FlyerPreviewBusinessHourResponse(it) },
            marketPhone1 = market.phoneNumber.marketPhone1,
            marketPhone2 = market.phoneNumber.marketPhone2,
            ownerPhone = market.phoneNumber.ownerPhone,
            top3 = top3.map { FlyerPreviewProductResponse(it) },
            daily = FlyerPreviewDailyResponse(dailyProducts),
            preparedProducts = preparedProducts.map { FlyerPreviewPreparedProductResponse(it) },
        )
    }

    @Transactional(readOnly = true)
    fun getDailyPreview(
        ownerId: Long,
        marketId: Long,
        now: LocalDateTime,
    ): FlyerDailyPreviewResponse {
        val market = marketService.getByIdForOwner(ownerId, marketId)
        val today = now.toLocalDate()

        val top3 = productService.getPopularActiveProducts(marketId, today, TOP_PRODUCTS_LIMIT)
        val dailyProducts = productService.getAllActiveProducts(marketId, DealType.DAILY, today)

        return FlyerDailyPreviewResponse(
            marketId = market.id,
            name = market.info.name,
            thumbnailUrl = market.info.thumbnailUrl,
            address = market.info.address.substringBefore("|"),
            isOpenNow = market.businessHours.isOpenAt(now),
            businessHours = market.businessHours.slots.map { FlyerPreviewBusinessHourResponse(it) },
            marketPhone1 = market.phoneNumber.marketPhone1,
            marketPhone2 = market.phoneNumber.marketPhone2,
            ownerPhone = market.phoneNumber.ownerPhone,
            top3 = top3.map { FlyerPreviewProductResponse(it) },
            daily = FlyerPreviewDailyResponse(dailyProducts),
        )
    }

    companion object {
        private const val TOP_PRODUCTS_LIMIT = 3
    }
}
