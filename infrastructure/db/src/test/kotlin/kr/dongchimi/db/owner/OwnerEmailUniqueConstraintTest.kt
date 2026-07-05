package kr.dongchimi.db.owner

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import kr.dongchimi.db.testsupport.TestPostgresContainer
import kr.dongchimi.db.testsupport.insertOwner
import kr.dongchimi.db.testsupport.softDeleteOwner
import kr.dongchimi.db.testsupport.truncate
import org.postgresql.util.PSQLException
import java.sql.Connection

class OwnerEmailUniqueConstraintTest :
    FunSpec({
        lateinit var connection: Connection

        beforeSpec { connection = TestPostgresContainer.newConnection() }
        beforeEach { connection.truncate("owners") }
        afterSpec { connection.close() }

        test("활성 오너 2명이 같은 이메일로 가입하면 실패한다") {
            connection.insertOwner(email = "duplicate@dongchimi.kr")

            shouldThrow<PSQLException> {
                connection.insertOwner(email = "duplicate@dongchimi.kr")
            }
        }

        test("soft-delete된 오너와 같은 이메일로 재가입하면 성공한다") {
            val id = connection.insertOwner(email = "reused@dongchimi.kr")
            connection.softDeleteOwner(id)

            connection.insertOwner(email = "reused@dongchimi.kr")
        }

        test("서로 다른 이메일이면 둘 다 성공한다") {
            connection.insertOwner(email = "owner-a@dongchimi.kr")
            connection.insertOwner(email = "owner-b@dongchimi.kr")
        }
    })
