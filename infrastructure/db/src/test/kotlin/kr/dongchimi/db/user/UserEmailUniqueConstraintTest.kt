package kr.dongchimi.db.user

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import kr.dongchimi.db.testsupport.TestPostgresContainer
import kr.dongchimi.db.testsupport.insertUser
import kr.dongchimi.db.testsupport.softDeleteUser
import kr.dongchimi.db.testsupport.truncate
import org.postgresql.util.PSQLException
import java.sql.Connection

class UserEmailUniqueConstraintTest :
    FunSpec({
        lateinit var connection: Connection

        beforeSpec { connection = TestPostgresContainer.newConnection() }
        beforeEach { connection.truncate("users") }
        afterSpec { connection.close() }

        test("활성 유저 2명이 같은 이메일로 가입하면 실패한다") {
            connection.insertUser(email = "duplicate@dongchimi.kr")

            shouldThrow<PSQLException> {
                connection.insertUser(email = "duplicate@dongchimi.kr")
            }
        }

        test("soft-delete된 유저와 같은 이메일로 재가입하면 성공한다") {
            val id = connection.insertUser(email = "reused@dongchimi.kr")
            connection.softDeleteUser(id)

            connection.insertUser(email = "reused@dongchimi.kr")
        }

        test("서로 다른 이메일이면 둘 다 성공한다") {
            connection.insertUser(email = "user-a@dongchimi.kr")
            connection.insertUser(email = "user-b@dongchimi.kr")
        }
    })
