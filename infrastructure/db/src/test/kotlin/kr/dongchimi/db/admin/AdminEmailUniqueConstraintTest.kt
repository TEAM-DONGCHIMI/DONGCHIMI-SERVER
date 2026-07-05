package kr.dongchimi.db.admin

import io.kotest.assertions.throwables.shouldThrow
import kr.dongchimi.db.testsupport.ConstraintSpec
import kr.dongchimi.db.testsupport.insertAdmin
import org.postgresql.util.PSQLException

class AdminEmailUniqueConstraintTest :
    ConstraintSpec(tableName = "admins", body = {
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
