package kr.dongchimi.infrastructure.redis

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kr.dongchimi.core.product.ImportJobCancelSignal
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration

/**
 * request()는 플래그(SET)와 control 채널 PUBLISH를 함께 한다 — 플래그는 워커가 체크포인트에서
 * 폴링하는 확실한 경로, PUBLISH는 활성 워커가 즉시 반응하는 fast path다(계획서 §3-2).
 * 워커 쪽에서 control 채널을 구독해 즉시 반응하는 부분은 워커 구현 시점에 추가한다.
 */
@Component
class RedisImportJobCancelSignal(
    private val stringRedisTemplate: StringRedisTemplate,
) : ImportJobCancelSignal {
    override suspend fun request(jobId: String) {
        withContext(Dispatchers.IO) {
            stringRedisTemplate.opsForValue().set(ImportJobRedisKeys.cancel(jobId), CANCEL_FLAG_VALUE, FLAG_TTL)
            stringRedisTemplate.convertAndSend(ImportJobRedisKeys.control(jobId), CANCEL_SIGNAL)
        }
    }

    override suspend fun isRequested(jobId: String): Boolean =
        withContext(Dispatchers.IO) {
            stringRedisTemplate.hasKey(ImportJobRedisKeys.cancel(jobId)) ?: false
        }

    companion object {
        private val FLAG_TTL = Duration.ofHours(1)
        private const val CANCEL_FLAG_VALUE = "1"
        private const val CANCEL_SIGNAL = "CANCEL"
    }
}
