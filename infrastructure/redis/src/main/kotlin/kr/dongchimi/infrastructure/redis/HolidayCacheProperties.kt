package kr.dongchimi.infrastructure.redis

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "holiday-cache")
data class HolidayCacheProperties(
    /** 공휴일 캐시 TTL. 임시공휴일 지정이 늦지 않게 반영되도록 1일 수준을 권장. */
    val ttl: Duration,
    /** API 장애 시 빈 값을 캐싱하는 짧은 TTL. 요청마다 외부 API를 재시도하지 않게 한다. */
    val fallbackTtl: Duration,
)
