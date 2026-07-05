package kr.dongchimi.db.admin

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import kr.dongchimi.db.testsupport.TestPostgresContainer
import kr.dongchimi.db.testsupport.insertAdmin
import kr.dongchimi.db.testsupport.truncate
import org.postgresql.util.PSQLException
import java.sql.Connection

class AdminEmailUniqueConstraintTest :
    FunSpec({
        lateinit var connection: Connection

        beforeSpec { connection = TestPostgresContainer.newConnection() }
        beforeEach { connection.truncate("admins") }
        afterSpec { connection.close() }

        test("동일한 이메일의 admin을 2명 생성하면 실패한다") {
            connection.insertAdmin(email = "duplicate@dongchimi.kr")

            shouldThrow<PSQLException> {
                connection.insertAdmin(email = "duplicate@dongchimi.kr")
            }
        }

        test("서로 다른 이메일이면 둘 다 성공한다") {
            connection.insertAdmin(email = "admin-a@dongchimi.kr")
            connection.insertAdmin(email = "admin-b@dongchimi.kr")
        }
    })
