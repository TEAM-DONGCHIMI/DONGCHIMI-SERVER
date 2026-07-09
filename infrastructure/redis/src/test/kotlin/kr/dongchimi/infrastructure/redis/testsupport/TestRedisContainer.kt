package kr.dongchimi.infrastructure.redis.testsupport

import org.testcontainers.containers.GenericContainer
import org.testcontainers.utility.DockerImageName

/**
 * 전체 테스트 스펙이 공유하는 싱글턴 Redis 컨테이너.
 * object의 클래스 초기화는 JVM에 의해 1회만 수행되므로 컨테이너 기동도 1회만 실행된다.
 */
object TestRedisContainer {
    private const val REDIS_PORT = 6379

    val container: GenericContainer<*> =
        GenericContainer(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(REDIS_PORT)

    init {
        container.start()
    }

    fun host(): String = container.host

    fun port(): Int = container.getMappedPort(REDIS_PORT)
}
