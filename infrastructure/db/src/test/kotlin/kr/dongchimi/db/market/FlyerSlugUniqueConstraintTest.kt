package kr.dongchimi.db.market

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import kr.dongchimi.db.testsupport.TestPostgresContainer
import kr.dongchimi.db.testsupport.insertFlyer
import kr.dongchimi.db.testsupport.truncate
import org.postgresql.util.PSQLException
import java.sql.Connection

class FlyerSlugUniqueConstraintTest :
    FunSpec({
        lateinit var connection: Connection

        beforeSpec { connection = TestPostgresContainer.newConnection() }
        beforeEach { connection.truncate("flyers") }
        afterSpec { connection.close() }

        test("동일한 slug로 flyer 2개를 생성하면 실패한다") {
            connection.insertFlyer(marketId = 1L, slug = "duplicate-slug")

            shouldThrow<PSQLException> {
                connection.insertFlyer(marketId = 2L, slug = "duplicate-slug")
            }
        }

        test("서로 다른 slug면 둘 다 성공한다") {
            connection.insertFlyer(marketId = 1L, slug = "slug-a")
            connection.insertFlyer(marketId = 2L, slug = "slug-b")
        }
    })
