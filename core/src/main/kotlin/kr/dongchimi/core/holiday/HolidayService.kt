package kr.dongchimi.core.holiday

import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class HolidayService(
    private val holidayReader: HolidayReader,
) {
    /** baseDate 기준 영업중 판별에 필요한 공휴일 목록 (전날이 걸치는 연도까지 커버) */
    fun getHolidays(baseDate: LocalDate): Set<LocalDate> = holidayReader.getHolidays(baseDate)
}
