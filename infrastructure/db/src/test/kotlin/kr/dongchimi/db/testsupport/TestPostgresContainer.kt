package kr.dongchimi.db.testsupport

import org.flywaydb.core.Flyway
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName
import java.sql.Connection
import java.sql.DriverManager

/**
 * 전체 테스트 스펙이 공유하는 싱글턴 Postgres(PostGIS) 컨테이너.
 * V1__init.sql이 `CREATE EXTENSION postgis`를 사용하므로 순정 postgres 이미지는 쓸 수 없다.
 * object의 클래스 초기화는 JVM에 의해 1회만 수행되므로 컨테이너 기동/마이그레이션도 1회만 실행된다.
 */
object TestPostgresContainer {
    private val container: PostgreSQLContainer<*> =
        PostgreSQLContainer(DockerImageName.parse("postgis/postgis:16-3.4").asCompatibleSubstituteFor("postgres"))
            .withDatabaseName("dongchimi_test")
            .withUsername("test")
            .withPassword("test")

    init {
        container.start()
        Flyway
            .configure()
            .dataSource(container.jdbcUrl, container.username, container.password)
            .locations("classpath:db/migration")
            .load()
            .migrate()
    }

    fun newConnection(): Connection = DriverManager.getConnection(container.jdbcUrl, container.username, container.password)
}

fun Connection.truncate(vararg tables: String) {
    createStatement().use { statement ->
        statement.execute("TRUNCATE TABLE ${tables.joinToString(", ")} RESTART IDENTITY CASCADE")
    }
}
