package kr.dongchimi.core.market

import java.time.DayOfWeek
import java.time.LocalTime

data class BusinessHourSlot(
    val days: List<DayOfWeek>,
    val isOpen: Boolean,
    val open: LocalTime? = null,
    val close: LocalTime? = null,
)
