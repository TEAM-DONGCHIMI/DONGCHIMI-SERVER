package kr.dongchimi.infrastructure.redis

import kr.dongchimi.core.auth.AccountCache
import kr.dongchimi.core.auth.AccountCacheProperties
import kr.dongchimi.core.auth.Role
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component

@Component
class RedisAccountCache(
    private val stringRedisTemplate: StringRedisTemplate,
    private val properties: AccountCacheProperties,
) : AccountCache {
    // Redis 장애 시 miss로 간주 → 각 Reader가 DB로 fallback한다(fail-open to DB). 인증 흐름이 500으로 깨지지 않는다.
    override fun isKnownToExist(
        role: Role,
        id: Long,
    ): Boolean =
        runCatching { stringRedisTemplate.hasKey(AccountCacheRedisKeys.exists(role, id)) == true }
            .getOrDefault(false)

    override fun markExists(
        role: Role,
        id: Long,
    ) {
        runCatching {
            stringRedisTemplate.opsForValue().set(AccountCacheRedisKeys.exists(role, id), EXISTS_VALUE, properties.ttl)
        }
    }

    override fun evict(
        role: Role,
        id: Long,
    ) {
        runCatching { stringRedisTemplate.delete(AccountCacheRedisKeys.exists(role, id)) }
    }

    companion object {
        private const val EXISTS_VALUE = "1"
    }
}
