package kr.dongchimi.core.holiday

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import java.time.LocalDate

private val logger = KotlinLogging.logger {}

@Component
class HolidayReader(
    private val holidayCache: HolidayCache,
    private val holidayClient: HolidayClient,
) {
    /** 자정 넘김 영업 판별이 전날 공휴일 여부까지 보므로 전날이 걸치는 연도도 함께 조회한다. */
    fun getHolidays(baseDate: LocalDate): Set<LocalDate> {
        val years = setOf(baseDate.year, baseDate.minusDays(1).year)
        return years.flatMap { holidaysOf(it) }.toSet()
    }

    private fun holidaysOf(year: Int): Set<LocalDate> = holidayCache.get(year) ?: fetchAndCache(year)

    // 캐시·API 모두 실패하면 공휴일 없음으로 간주한다. isOpenNow는 부가 정보라 조회 자체를 깨지 않는다.
    private fun fetchAndCache(year: Int): Set<LocalDate> =
        runCatching { holidayClient.fetchHolidays(year) }
            .onSuccess { holidayCache.put(year, it) }
            .getOrElse { exception ->
                logger.warn(exception) { "공휴일 조회 실패, 공휴일 없음으로 간주: year=$year" }
                holidayCache.putFallback(year)
                emptySet()
            }
}
