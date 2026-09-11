package kr.dongchimi.core.holiday

import java.time.LocalDate

interface HolidayCache {
    /** null이면 미캐싱, 빈 Set이면 "공휴일 없음"으로 캐싱된 상태 */
    fun get(year: Int): Set<LocalDate>?

    fun put(
        year: Int,
        holidays: Set<LocalDate>,
    )

    /** API 장애 시 짧은 TTL로 빈 값을 캐싱해 요청마다 외부 API를 재시도하지 않게 한다. */
    fun putFallback(year: Int)
}
