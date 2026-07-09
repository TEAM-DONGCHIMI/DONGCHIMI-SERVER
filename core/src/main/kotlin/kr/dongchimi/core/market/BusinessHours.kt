package kr.dongchimi.core.market

import java.time.LocalDateTime

data class BusinessHours(
    val slots: List<BusinessHourSlot>,
) {
    fun isOpenAt(dateTime: LocalDateTime): Boolean {
        val slot = slots.firstOrNull { dateTime.dayOfWeek in it.days } ?: return false
        if (!slot.isOpen || slot.open == null || slot.close == null) return false

        val now = dateTime.toLocalTime()
        return !now.isBefore(slot.open) && now.isBefore(slot.close)
    }
}
