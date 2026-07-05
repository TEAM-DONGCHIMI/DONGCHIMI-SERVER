package kr.dongchimi.db.owner

import io.kotest.assertions.throwables.shouldThrow
import kr.dongchimi.db.testsupport.ConstraintSpec
import kr.dongchimi.db.testsupport.insertOwner
import kr.dongchimi.db.testsupport.softDeleteOwner
import org.postgresql.util.PSQLException

class OwnerEmailUniqueConstraintTest :
    ConstraintSpec(tableName = "owners", body = {
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
