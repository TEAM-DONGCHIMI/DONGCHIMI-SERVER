package kr.dongchimi.core.market

import java.time.DayOfWeek
import java.time.LocalTime

data class BusinessHourSlot(
    val days: List<DayOfWeek>,
    val isOpen: Boolean,
    val open: LocalTime? = null,
    val close: LocalTime? = null,
) {
    // 22:00~02:00처럼 마감이 다음 날로 넘어가는 슬롯. open == close는 자정 넘김으로 보지 않는다.
    // is 접두사를 쓰면 Jackson이 getter로 인식해 business_hours JSONB에 필드가 새어 나간다.
    fun crossesMidnight(): Boolean = isOpen && open != null && close != null && close < open

    fun contains(time: LocalTime): Boolean =
        when {
            !isOpen || open == null || close == null -> false
            crossesMidnight() -> time >= open
            else -> time >= open && time < close
        }

    fun containsOvernightTail(time: LocalTime): Boolean = crossesMidnight() && time < close!!
}
