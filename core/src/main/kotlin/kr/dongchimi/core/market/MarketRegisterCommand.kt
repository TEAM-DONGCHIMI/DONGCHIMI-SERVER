package kr.dongchimi.core.market

data class MarketRegisterCommand(
    val info: MarketInfo,
    val location: LocationPoint,
    val businessHours: BusinessHours?,
    val phoneNumber: MarketPhoneNumber,
    val brn: String?,
)
