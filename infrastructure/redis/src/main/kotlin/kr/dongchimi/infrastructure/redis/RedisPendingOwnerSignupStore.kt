package kr.dongchimi.infrastructure.redis

import kr.dongchimi.core.owner.PendingOwnerSignup
import kr.dongchimi.core.owner.PendingOwnerSignupStore
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper
import java.time.Duration

@Component
class RedisPendingOwnerSignupStore(
    private val stringRedisTemplate: StringRedisTemplate,
    private val objectMapper: ObjectMapper,
) : PendingOwnerSignupStore {
    override fun save(
        signupToken: String,
        pending: PendingOwnerSignup,
    ) {
        stringRedisTemplate
            .opsForValue()
            .set(OwnerSignupRedisKeys.pending(signupToken), objectMapper.writeValueAsString(pending), PENDING_SIGNUP_TTL)
    }

    override fun consume(signupToken: String): PendingOwnerSignup? =
        stringRedisTemplate
            .opsForValue()
            .getAndDelete(OwnerSignupRedisKeys.pending(signupToken))
            ?.let { objectMapper.readValue(it, PendingOwnerSignup::class.java) }

    companion object {
        private val PENDING_SIGNUP_TTL = Duration.ofMinutes(30)
    }
}
