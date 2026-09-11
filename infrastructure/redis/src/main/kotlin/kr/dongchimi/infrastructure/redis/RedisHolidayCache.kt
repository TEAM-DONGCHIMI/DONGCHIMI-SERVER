package kr.dongchimi.infrastructure.redis

import kr.dongchimi.core.holiday.HolidayCache
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Component
class RedisHolidayCache(
    private val stringRedisTemplate: StringRedisTemplate,
    private val properties: HolidayCacheProperties,
) : HolidayCache {
    // Redis 장애 시 miss로 간주 → HolidayReader가 외부 API 조회로 fallback한다(fail-open).
    override fun get(year: Int): Set<LocalDate>? =
        runCatching {
            stringRedisTemplate.opsForValue().get(HolidayRedisKeys.holidays(year))?.toHolidays()
        }.getOrNull()

    override fun put(
        year: Int,
        holidays: Set<LocalDate>,
    ) {
        runCatching {
            stringRedisTemplate.opsForValue().set(HolidayRedisKeys.holidays(year), holidays.toValue(), properties.ttl)
        }
    }

    override fun putFallback(year: Int) {
        runCatching {
            stringRedisTemplate.opsForValue().set(HolidayRedisKeys.holidays(year), EMPTY_VALUE, properties.fallbackTtl)
        }
    }

    private fun Set<LocalDate>.toValue(): String = joinToString(SEPARATOR) { it.format(DateTimeFormatter.BASIC_ISO_DATE) }

    private fun String.toHolidays(): Set<LocalDate> =
        if (isEmpty()) {
            emptySet()
        } else {
            split(SEPARATOR).map { LocalDate.parse(it, DateTimeFormatter.BASIC_ISO_DATE) }.toSet()
        }

    companion object {
        private const val SEPARATOR = ","
        private const val EMPTY_VALUE = ""
    }
}
