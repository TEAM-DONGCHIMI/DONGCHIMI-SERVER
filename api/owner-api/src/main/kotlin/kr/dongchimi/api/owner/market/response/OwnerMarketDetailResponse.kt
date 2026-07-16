package kr.dongchimi.api.owner.market.response

import io.swagger.v3.oas.annotations.media.Schema
import kr.dongchimi.core.market.Market

data class OwnerMarketDetailResponse(
    @Schema(description = "마트 id")
    val marketId: Long,
    @Schema(description = "마트명")
    val name: String,
    @Schema(description = "마트 대표 이미지 URL (없으면 null)")
    val thumbnailUrl: String?,
    @Schema(description = "마트 주소")
    val address: String,
    @Schema(description = "위도")
    val latitude: Double,
    @Schema(description = "경도")
    val longitude: Double,
    @Schema(description = "영업시간 (요일 묶음 배열)")
    val businessHours: List<OwnerMarketBusinessHourResponse>,
    @Schema(description = "공휴일 휴무 여부")
    val isHolidayClosed: Boolean,
    @Schema(description = "마트 대표 전화번호 1")
    val marketPhone1: String,
    @Schema(description = "마트 전화번호 2 (없으면 null)")
    val marketPhone2: String?,
    @Schema(description = "마트 번호 중 대표 번호 (1 또는 2)")
    val marketPhonePrimary: Short,
    @Schema(description = "점주 전화번호")
    val ownerPhone: String,
    @Schema(description = "사업자등록번호 (없으면 null)")
    val brn: String?,
) {
    constructor(market: Market) : this(
        marketId = market.id,
        name = market.info.name,
        thumbnailUrl = market.info.thumbnailUrl,
        address = market.info.address,
        latitude = market.location.latitude,
        longitude = market.location.longitude,
        businessHours = market.businessHours.slots.map { OwnerMarketBusinessHourResponse(it) },
        isHolidayClosed = market.businessHours.isHolidayClosed,
        marketPhone1 = market.phoneNumber.marketPhone1,
        marketPhone2 = market.phoneNumber.marketPhone2,
        marketPhonePrimary = market.phoneNumber.marketPhonePrimary,
        ownerPhone = market.phoneNumber.ownerPhone,
        brn = market.brn,
    )
}
