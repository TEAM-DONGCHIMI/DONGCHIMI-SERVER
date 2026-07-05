package kr.dongchimi.db.user

import io.kotest.assertions.throwables.shouldThrow
import kr.dongchimi.db.testsupport.ConstraintSpec
import kr.dongchimi.db.testsupport.insertUser
import kr.dongchimi.db.testsupport.softDeleteUser
import org.postgresql.util.PSQLException

class UserEmailUniqueConstraintTest :
    ConstraintSpec(tableName = "users", body = {
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
