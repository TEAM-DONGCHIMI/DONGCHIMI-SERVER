package kr.dongchimi.core.market

data class BusinessHours(
    val monday: DaySchedule? = null,
    val tuesday: DaySchedule? = null,
    val wednesday: DaySchedule? = null,
    val thursday: DaySchedule? = null,
    val friday: DaySchedule? = null,
    val saturday: DaySchedule? = null,
    val sunday: DaySchedule? = null,
)
