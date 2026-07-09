package kr.dongchimi.infrastructure.redis

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kr.dongchimi.infrastructure.redis.testsupport.TestRedisContainer
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.StringRedisTemplate

class RedisConnectionTest :
    FunSpec({
        val connectionFactory =
            LettuceConnectionFactory(
                RedisStandaloneConfiguration(TestRedisContainer.host(), TestRedisContainer.port()),
            )
        connectionFactory.afterPropertiesSet()

        val redisTemplate = StringRedisTemplate(connectionFactory)

        test("REDIS_HOST/REDIS_PORT로 구성한 연결로 set/get이 왕복한다") {
            redisTemplate.opsForValue().set("import:job:test", "hello")

            redisTemplate.opsForValue().get("import:job:test") shouldBe "hello"
        }

        test("존재하지 않는 키를 조회하면 null을 반환한다") {
            redisTemplate.opsForValue().get("import:job:missing").shouldBeNull()
        }
    })
