package kr.dongchimi.core.holiday

import java.time.LocalDate

interface HolidayClient {
    /** 해당 연도의 공휴일 목록을 외부 API에서 조회한다. 실패 시 예외를 던진다. */
    fun fetchHolidays(year: Int): Set<LocalDate>
}
