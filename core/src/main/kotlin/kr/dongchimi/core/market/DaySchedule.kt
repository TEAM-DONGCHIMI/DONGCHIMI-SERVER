package kr.dongchimi.core.market

import java.time.LocalTime

data class DaySchedule(
    val openTime: LocalTime,
    val closeTime: LocalTime,
)
