package kr.dongchimi.core.market

data class Market(
    val id: Long = 0,
    val ownerId: Long,
    val name: String,
    val address: String,
    val thumbnailUrl: String?,
    val location: LocationPoint,
    val businessHours: BusinessHours?,
    val phoneNumber: MarketPhoneNumber,
    val brn: String?,
)
