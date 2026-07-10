package kr.dongchimi.bootstrap

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

@SpringBootTest(
    properties = [
        "jwt.secret-key=dongchimi-test-secret-key-for-context-load",
        "jwt.issuer=dongchimi-test",
        "cors.allowed-origins=http://localhost",
        "cors.allowed-methods=GET,POST,PUT,PATCH,DELETE,OPTIONS",
        "cors.allowed-headers=*",
        "storage.cdn-base-url=https://cdn.example.com",
        "storage.s3.bucket=dongchimi-test",
    ],
)
class DongchimiServerApplicationTests {
    @Test
    fun contextLoads() {
    }

    companion object {
        private val postgres =
            PostgreSQLContainer<Nothing>(
                DockerImageName
                    .parse("postgis/postgis:16-3.4")
                    .asCompatibleSubstituteFor("postgres"),
            ).apply {
                start()
            }

        private val redis =
            GenericContainer(DockerImageName.parse("redis:7-alpine")).apply {
                withExposedPorts(6379)
                start()
            }

        @JvmStatic
        @DynamicPropertySource
        fun registerProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
            registry.add("spring.data.redis.host", redis::getHost)
            registry.add("spring.data.redis.port") { redis.getMappedPort(6379) }
        }
    }
}
