package kr.dongchimi.db.auth

import io.kotest.assertions.throwables.shouldThrow
import kr.dongchimi.db.testsupport.ConstraintSpec
import kr.dongchimi.db.testsupport.insertRefreshToken
import org.postgresql.util.PSQLException

class RefreshTokenSchemaTest :
    ConstraintSpec(tableName = "refresh_tokens", body = {
        test("같은 user_id로 여러 세션(row)을 저장할 수 있다") {
            connection.insertRefreshToken(tokenId = "token-1", userId = 1L)
            connection.insertRefreshToken(tokenId = "token-2", userId = 1L)
        }

        test("같은 token_id로 두 번 저장하면 실패한다") {
            connection.insertRefreshToken(tokenId = "token-1", userId = 1L)

            shouldThrow<PSQLException> {
                connection.insertRefreshToken(tokenId = "token-1", userId = 2L)
            }
        }
    })
