package kr.dongchimi.core.market

import java.time.DayOfWeek
import java.time.LocalDateTime

data class BusinessHours(
    val slots: List<BusinessHourSlot>,
) {
    fun isOpenAt(dateTime: LocalDateTime): Boolean {
        val time = dateTime.toLocalTime()

        return slotsOf(dateTime.dayOfWeek).any { it.contains(time) } ||
            slotsOf(dateTime.dayOfWeek.minus(1)).any { it.containsOvernightTail(time) }
    }

    private fun slotsOf(dayOfWeek: DayOfWeek): List<BusinessHourSlot> = slots.filter { dayOfWeek in it.days }
}
