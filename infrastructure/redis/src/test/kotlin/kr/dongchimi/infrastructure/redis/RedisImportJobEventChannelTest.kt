package kr.dongchimi.infrastructure.redis

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import kr.dongchimi.core.product.ImportJobEvent
import kr.dongchimi.core.product.ImportStep
import kr.dongchimi.infrastructure.redis.testsupport.TestRedisContainer
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.listener.RedisMessageListenerContainer
import tools.jackson.module.kotlin.jacksonObjectMapper
import java.util.UUID
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class RedisImportJobEventChannelTest :
    FunSpec({
        val connectionFactory =
            LettuceConnectionFactory(RedisStandaloneConfiguration(TestRedisContainer.host(), TestRedisContainer.port()))
        connectionFactory.afterPropertiesSet()
        val listenerContainer =
            RedisMessageListenerContainer().apply {
                setConnectionFactory(connectionFactory)
                afterPropertiesSet()
                start()
            }
        val channel = RedisImportJobEventChannel(StringRedisTemplate(connectionFactory), listenerContainer, jacksonObjectMapper())

        test("subscribe(jobId)가 publish한 이벤트를 수신한다") {
            val jobId = "imp_${UUID.randomUUID()}"

            coroutineScope {
                val received = async { channel.subscribe(jobId).first() }
                delay(300) // addMessageListener가 비동기라 실제 구독이 걸릴 시간을 준다

                channel.publish(ImportJobEvent.Canceled(jobId))

                withTimeout(5.seconds) {
                    received.await() shouldBe ImportJobEvent.Canceled(jobId)
                }
            }
        }

        test("다른 jobId로 publish한 이벤트는 받지 않는다") {
            val jobId = "imp_${UUID.randomUUID()}"
            val otherJobId = "imp_${UUID.randomUUID()}"

            coroutineScope {
                val collecting = async { channel.subscribe(jobId).first() }
                delay(300)

                channel.publish(ImportJobEvent.Canceled(otherJobId))

                withTimeoutOrNull(500.milliseconds) { collecting.await() }.shouldBeNull()
                collecting.cancel()
            }
        }

        test("progress 이벤트가 필드를 그대로 왕복한다") {
            val jobId = "imp_${UUID.randomUUID()}"
            val event =
                ImportJobEvent.Progress(
                    jobId = jobId,
                    progress = 40,
                    remainingSeconds = 20,
                    currentStep = ImportStep.PRICE_EXTRACTION,
                    steps = emptyList(),
                )

            coroutineScope {
                val received = async { channel.subscribe(jobId).first() }
                delay(300)

                channel.publish(event)

                withTimeout(5.seconds) {
                    received.await() shouldBe event
                }
            }
        }
    })
