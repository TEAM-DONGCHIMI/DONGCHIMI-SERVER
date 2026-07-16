package kr.dongchimi.api.owner.flyer.response

import io.swagger.v3.oas.annotations.media.Schema
import kr.dongchimi.core.market.Market
import kr.dongchimi.core.product.Product
import java.time.LocalDateTime

data class FlyerDailyPreviewResponse(
    @Schema(description = "마트 id")
    val marketId: Long,
    @Schema(description = "마트명")
    val name: String,
    @Schema(description = "마트 썸네일 이미지 URL")
    val thumbnailUrl: String?,
    @Schema(description = "마트 기본 주소")
    val address: String,
    @Schema(description = "현재 영업중 여부")
    val isOpenNow: Boolean,
    @Schema(description = "영업시간 (요일 묶음 배열)")
    val businessHours: List<FlyerPreviewBusinessHourResponse>,
    @Schema(description = "공휴일 휴무 여부")
    val isHolidayClosed: Boolean,
    @Schema(description = "마트 대표 전화번호 1")
    val marketPhone1: String,
    @Schema(description = "마트 전화번호 2 (없으면 null)")
    val marketPhone2: String?,
    @Schema(description = "사장님 전화번호")
    val ownerPhone: String,
    @Schema(description = "인기 상품 TOP 3")
    val top3: List<FlyerPreviewProductResponse>,
    @Schema(description = "오늘의 특가")
    val daily: FlyerPreviewDailyResponse,
) {
    constructor(
        market: Market,
        now: LocalDateTime,
        top3: List<Product>,
        dailyProducts: List<Product>,
    ) : this(
        marketId = market.id,
        name = market.info.name,
        thumbnailUrl = market.info.thumbnailUrl,
        address = market.info.address.substringBefore("|"),
        isOpenNow = market.businessHours.isOpenAt(now),
        businessHours = market.businessHours.slots.map { FlyerPreviewBusinessHourResponse(it) },
        isHolidayClosed = market.businessHours.isHolidayClosed,
        marketPhone1 = market.phoneNumber.marketPhone1,
        marketPhone2 = market.phoneNumber.marketPhone2,
        ownerPhone = market.phoneNumber.ownerPhone,
        top3 = top3.map { FlyerPreviewProductResponse(it) },
        daily = FlyerPreviewDailyResponse(dailyProducts),
    )
}
