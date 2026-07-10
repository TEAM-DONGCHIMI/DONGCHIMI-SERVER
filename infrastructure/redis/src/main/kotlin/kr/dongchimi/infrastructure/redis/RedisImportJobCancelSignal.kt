package kr.dongchimi.infrastructure.redis

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import kr.dongchimi.core.product.import.ImportJobCancelSignal
import org.springframework.data.redis.connection.MessageListener
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.data.redis.listener.RedisMessageListenerContainer
import org.springframework.stereotype.Component
import java.time.Duration

/**
 * request()는 플래그(SET)와 control 채널 PUBLISH를 함께 한다 — 플래그는 워커가 체크포인트에서
 * 폴링하는 확실한 경로, PUBLISH는 활성 워커가 즉시 반응하는 fast path다(계획서 §3-2).
 */
@Component
class RedisImportJobCancelSignal(
    private val stringRedisTemplate: StringRedisTemplate,
    private val container: RedisMessageListenerContainer,
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

    override fun subscribeControl(jobId: String): Flow<Unit> =
        callbackFlow {
            val topic = ChannelTopic(ImportJobRedisKeys.control(jobId))
            val listener = MessageListener { _, _ -> trySend(Unit) }

            container.addMessageListener(listener, topic)

            awaitClose { container.removeMessageListener(listener, topic) }
        }

    companion object {
        private val FLAG_TTL = Duration.ofHours(1)
        private const val CANCEL_FLAG_VALUE = "1"
        private const val CANCEL_SIGNAL = "CANCEL"
    }
}
