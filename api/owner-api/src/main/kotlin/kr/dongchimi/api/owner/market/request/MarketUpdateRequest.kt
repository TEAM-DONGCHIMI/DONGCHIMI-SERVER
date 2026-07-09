package kr.dongchimi.api.owner.market.request

import io.swagger.v3.oas.annotations.media.Schema
import kr.dongchimi.core.market.LocationPoint
import kr.dongchimi.core.market.MarketInfo
import kr.dongchimi.core.market.MarketPhoneNumber
import kr.dongchimi.core.market.MarketUpdateCommand

data class MarketUpdateRequest(
    @Schema(description = "마트명", example = "동치미 마트 강남점")
    val name: String,
    @Schema(description = "마트 대표 이미지 URL. 미등록 시 기본 이미지 사용")
    val thumbnailUrl: String?,
    @Schema(description = "주소 찾기로 검색된 기본 주소")
    val address: String,
    @Schema(description = "상세 주소 (동/호수 등)")
    val detailAddress: String?,
    @Schema(description = "위도")
    val latitude: Double,
    @Schema(description = "경도")
    val longitude: Double,
    @Schema(description = "영업시간 (요일 묶음 배열)")
    val businessHours: List<BusinessHourSlotRequest>,
    @Schema(description = "마트 대표 전화번호 1")
    val marketPhone1: String,
    @Schema(description = "마트 전화번호 2 (추가 등록 시)")
    val marketPhone2: String?,
    @Schema(description = "마트 번호 중 대표 번호 지정 (1 또는 2)")
    val marketPhonePrimary: Short,
    @Schema(description = "점주 전화번호")
    val ownerPhone: String,
    @Schema(description = "사업자등록번호 (000-00-00000 형식)")
    val brn: String?,
) {
    fun toCommand(): MarketUpdateCommand {
        validateMarketFields(
            name,
            address,
            detailAddress,
            latitude,
            longitude,
            marketPhone1,
            marketPhone2,
            marketPhonePrimary,
            ownerPhone,
            brn,
        )

        return MarketUpdateCommand(
            info = MarketInfo(name = name, address = mergeAddress(address, detailAddress), thumbnailUrl = thumbnailUrl),
            location = LocationPoint(longitude = longitude, latitude = latitude),
            businessHours = businessHours.toBusinessHours(),
            phoneNumber = MarketPhoneNumber(marketPhone1, marketPhone2, marketPhonePrimary, ownerPhone),
            brn = brn,
        )
    }
}
