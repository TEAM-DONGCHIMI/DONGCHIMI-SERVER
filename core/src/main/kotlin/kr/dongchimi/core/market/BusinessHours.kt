package kr.dongchimi.core.market

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime

data class BusinessHours(
    val slots: List<BusinessHourSlot>,
    val isHolidayClosed: Boolean = false,
) {
    fun isOpenAt(
        dateTime: LocalDateTime,
        holidays: Set<LocalDate>,
    ): Boolean {
        val today = dateTime.toLocalDate()
        val time = dateTime.toLocalTime()

        // 전날이 공휴일 휴무면 자정을 넘긴 꼬리 영업도 휴무 처리한다.
        return (!closedOn(today, holidays) && slotsOf(today.dayOfWeek).any { it.contains(time) }) ||
            (!closedOn(today.minusDays(1), holidays) && slotsOf(today.dayOfWeek.minus(1)).any { it.containsOvernightTail(time) })
    }

    private fun closedOn(
        date: LocalDate,
        holidays: Set<LocalDate>,
    ): Boolean = isHolidayClosed && date in holidays

    private fun slotsOf(dayOfWeek: DayOfWeek): List<BusinessHourSlot> = slots.filter { dayOfWeek in it.days }
}
