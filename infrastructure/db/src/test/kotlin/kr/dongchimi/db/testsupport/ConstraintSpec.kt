package kr.dongchimi.db.testsupport

import io.kotest.core.spec.style.FunSpec
import java.sql.Connection

/**
 * unique 제약 검증 테스트가 공통으로 필요로 하는 라이프사이클(컨테이너 연결 생성/테이블 truncate/연결 종료)을 묶은 베이스 스펙.
 */
abstract class ConstraintSpec(
    tableName: String,
    body: ConstraintSpec.() -> Unit,
) : FunSpec() {
    lateinit var connection: Connection
        private set

    init {
        beforeSpec { connection = TestPostgresContainer.newConnection() }
        beforeEach { connection.truncate(tableName) }
        afterSpec { connection.close() }
        body()
    }
}
