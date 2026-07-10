package kr.dongchimi.infrastructure.redis

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout
import kr.dongchimi.infrastructure.redis.testsupport.TestRedisContainer
import org.springframework.data.redis.connection.MessageListener
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.data.redis.listener.RedisMessageListenerContainer
import java.util.UUID
import kotlin.time.Duration.Companion.seconds

class RedisImportJobCancelSignalTest :
    FunSpec({
        val connectionFactory =
            LettuceConnectionFactory(RedisStandaloneConfiguration(TestRedisContainer.host(), TestRedisContainer.port()))
        connectionFactory.afterPropertiesSet()
        val stringRedisTemplate = StringRedisTemplate(connectionFactory)
        val listenerContainer =
            RedisMessageListenerContainer().apply {
                setConnectionFactory(connectionFactory)
                afterPropertiesSet()
                start()
            }
        val signal = RedisImportJobCancelSignal(stringRedisTemplate)

        fun controlMessages(jobId: String) =
            callbackFlow {
                val listener = MessageListener { message, _ -> trySend(String(message.body)) }
                val topic = ChannelTopic(ImportJobRedisKeys.control(jobId))
                listenerContainer.addMessageListener(listener, topic)
                awaitClose { listenerContainer.removeMessageListener(listener, topic) }
            }

        test("request 전에는 isRequested가 false다") {
            signal.isRequested("imp_${UUID.randomUUID()}") shouldBe false
        }

        test("request 후 isRequested가 true다") {
            val jobId = "imp_${UUID.randomUUID()}"

            signal.request(jobId)

            signal.isRequested(jobId) shouldBe true
        }

        test("request는 control 채널로도 즉시 알린다 (활성 워커가 반응하는 fast path)") {
            val jobId = "imp_${UUID.randomUUID()}"

            coroutineScope {
                val received = async { controlMessages(jobId).first() }
                delay(300)

                signal.request(jobId)

                withTimeout(5.seconds) {
                    received.await() shouldBe "CANCEL"
                }
            }
        }
    })
