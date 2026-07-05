package kr.dongchimi.db.user

import io.kotest.assertions.throwables.shouldThrow
import kr.dongchimi.db.testsupport.ConstraintSpec
import kr.dongchimi.db.testsupport.insertUser
import kr.dongchimi.db.testsupport.softDeleteUser
import org.postgresql.util.PSQLException

class UserSocialAccountUniqueConstraintTest :
    ConstraintSpec(tableName = "users", body = {
        test("동일한 (social_provider, social_id) 조합으로 2명 가입하면 실패한다") {
            connection.insertUser(email = "a@dongchimi.kr", socialProvider = "KAKAO", socialId = "social-1")

            shouldThrow<PSQLException> {
                connection.insertUser(email = "b@dongchimi.kr", socialProvider = "KAKAO", socialId = "social-1")
            }
        }

        test("provider가 다르면 social_id가 같아도 성공한다") {
            connection.insertUser(email = "a@dongchimi.kr", socialProvider = "KAKAO", socialId = "social-1")
            connection.insertUser(email = "b@dongchimi.kr", socialProvider = "GOOGLE", socialId = "social-1")
        }

        test("soft-delete 후 동일한 (social_provider, social_id)로 재가입하면 성공한다") {
            val id = connection.insertUser(email = "a@dongchimi.kr", socialProvider = "KAKAO", socialId = "social-1")
            connection.softDeleteUser(id)

            connection.insertUser(email = "b@dongchimi.kr", socialProvider = "KAKAO", socialId = "social-1")
        }
    })
