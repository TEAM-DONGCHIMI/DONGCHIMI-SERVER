package kr.dongchimi.core.market

data class NearbyMarketSearchCondition(
    val origin: LocationPoint,
    val radiusMeters: Double,
    val cursorMarketId: Long?,
    val size: Int,
)
