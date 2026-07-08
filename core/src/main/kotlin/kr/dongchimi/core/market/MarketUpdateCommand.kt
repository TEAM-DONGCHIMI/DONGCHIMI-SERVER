package kr.dongchimi.core.market

data class MarketUpdateCommand(
    val info: MarketInfo,
    val location: LocationPoint,
    val businessHours: BusinessHours,
    val phoneNumber: MarketPhoneNumber,
    val brn: String?,
)
