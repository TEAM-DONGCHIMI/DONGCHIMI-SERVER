package kr.dongchimi.infrastructure.redis

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import kr.dongchimi.core.product.ImportJobEvent
import kr.dongchimi.core.product.ImportJobEventChannel
import org.springframework.data.redis.connection.MessageListener
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.data.redis.listener.RedisMessageListenerContainer
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper

@Component
class RedisImportJobEventChannel(
    private val stringRedisTemplate: StringRedisTemplate,
    private val container: RedisMessageListenerContainer,
    private val objectMapper: ObjectMapper,
) : ImportJobEventChannel {
    override suspend fun publish(event: ImportJobEvent) {
        withContext(Dispatchers.IO) {
            val json = objectMapper.writeValueAsString(ImportJobEventEnvelope.from(event))
            stringRedisTemplate.convertAndSend(ImportJobRedisKeys.events(event.jobId), json)
        }
    }

    override fun subscribe(jobId: String): Flow<ImportJobEvent> =
        callbackFlow {
            val topic = ChannelTopic(ImportJobRedisKeys.events(jobId))
            val listener =
                MessageListener { message, _ ->
                    val envelope = objectMapper.readValue(message.body, ImportJobEventEnvelope::class.java)
                    trySend(envelope.toDomain())
                }

            container.addMessageListener(listener, topic)

            awaitClose { container.removeMessageListener(listener, topic) }
        }
}
