package kr.dongchimi.infrastructure.redis

internal object HolidayRedisKeys {
    // holiday:2026
    fun holidays(year: Int) = "holiday:$year"
}
