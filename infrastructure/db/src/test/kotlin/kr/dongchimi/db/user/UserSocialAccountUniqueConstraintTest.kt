package kr.dongchimi.db.user

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import kr.dongchimi.db.testsupport.TestPostgresContainer
import kr.dongchimi.db.testsupport.insertUser
import kr.dongchimi.db.testsupport.softDeleteUser
import kr.dongchimi.db.testsupport.truncate
import org.postgresql.util.PSQLException
import java.sql.Connection

class UserSocialAccountUniqueConstraintTest :
    FunSpec({
        lateinit var connection: Connection

        beforeSpec { connection = TestPostgresContainer.newConnection() }
        beforeEach { connection.truncate("users") }
        afterSpec { connection.close() }

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

        test("social_id가 NULL인 유저는 여러 명 가입해도 성공한다") {
            connection.insertUser(email = "a@dongchimi.kr", socialProvider = "KAKAO", socialId = null)
            connection.insertUser(email = "b@dongchimi.kr", socialProvider = "KAKAO", socialId = null)
        }

        test("soft-delete 후 동일한 (social_provider, social_id)로 재가입하면 성공한다") {
            val id = connection.insertUser(email = "a@dongchimi.kr", socialProvider = "KAKAO", socialId = "social-1")
            connection.softDeleteUser(id)

            connection.insertUser(email = "b@dongchimi.kr", socialProvider = "KAKAO", socialId = "social-1")
        }
    })
