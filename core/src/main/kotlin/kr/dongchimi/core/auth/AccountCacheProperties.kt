package kr.dongchimi.core.auth

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "account-cache")
data class AccountCacheProperties(
    /** 계정 존재 캐시 TTL. 탈퇴 evict가 없더라도 이 기간 내에 스테일이 정리된다. */
    val ttl: Duration,
)
