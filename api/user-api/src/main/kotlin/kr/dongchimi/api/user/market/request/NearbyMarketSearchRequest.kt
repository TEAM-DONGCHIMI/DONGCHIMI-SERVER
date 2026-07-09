package kr.dongchimi.api.user.market.request

import kr.dongchimi.api.core.common.exception.validate
import kr.dongchimi.core.market.LocationPoint
import kr.dongchimi.core.market.NearbyMarketSearchCondition
import org.springdoc.core.annotations.ParameterObject

@ParameterObject
data class NearbyMarketSearchRequest(
    val lat: Double? = null,
    val lng: Double? = null,
    val radius: Double? = null,
    val cursor: Long? = null,
    val size: Int? = null,
) {
    fun toSearchCondition(): NearbyMarketSearchCondition {
        validate(lat != null) { "위도는 필수로 입력해 주세요." }
        validate(lng != null) { "경도는 필수로 입력해 주세요." }
        validate(lat!! in MIN_LATITUDE..MAX_LATITUDE) { "경도, 위도가 유효 범위를 벗어났습니다." }
        validate(lng!! in MIN_LONGITUDE..MAX_LONGITUDE) { "경도, 위도가 유효 범위를 벗어났습니다." }

        val radius = radius ?: DEFAULT_RADIUS_METERS
        val size = size ?: DEFAULT_SIZE
        validate(radius > 0) { "반경은 0보다 커야 합니다." }
        validate(size > 0) { "조회 개수는 1 이상이어야 합니다." }

        return NearbyMarketSearchCondition(
            origin = LocationPoint(longitude = lng, latitude = lat),
            radiusMeters = radius,
            cursorMarketId = cursor,
            size = size,
        )
    }

    companion object {
        private const val MIN_LATITUDE = -90.0
        private const val MAX_LATITUDE = 90.0
        private const val MIN_LONGITUDE = -180.0
        private const val MAX_LONGITUDE = 180.0
        private const val DEFAULT_RADIUS_METERS = 1000.0
        private const val DEFAULT_SIZE = 5
    }
}
