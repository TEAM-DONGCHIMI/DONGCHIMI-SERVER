package kr.dongchimi.core.auth

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kr.dongchimi.core.common.exception.CoreException
import java.time.LocalDateTime

class RefreshTokenRotatorTest :
    FunSpec({
        test("유효한 토큰이면 old를 삭제하고 new를 저장한다") {
            val repository = FakeRefreshTokenRepository()
            val oldToken = RefreshToken(tokenId = "old-id", userId = 1L, expiresAt = LocalDateTime.now())
            repository.save(oldToken)
            val issued = IssuedRefreshToken(tokenValue = "new-value", tokenId = "new-id", expiresAt = LocalDateTime.now().plusDays(14))

            val rotated = RefreshTokenRotator(repository).rotate(oldTokenId = "old-id", issued = issued, userId = 1L)

            rotated shouldBe RefreshToken(issued, 1L)
            repository.contains("old-id") shouldBe false
            repository.contains("new-id") shouldBe true
        }

        test("이미 폐기되었거나 존재하지 않는 토큰이면 예외가 발생한다") {
            val repository = FakeRefreshTokenRepository()
            val issued = IssuedRefreshToken(tokenValue = "new-value", tokenId = "new-id", expiresAt = LocalDateTime.now().plusDays(14))

            val exception =
                shouldThrow<CoreException> {
                    RefreshTokenRotator(repository).rotate(oldTokenId = "unknown-id", issued = issued, userId = 1L)
                }

            exception.errorCode shouldBe AuthErrorCode.INVALID_REFRESH_TOKEN
            repository.contains("new-id") shouldBe false
        }
    }) {
    private class FakeRefreshTokenRepository : RefreshTokenRepository {
        private val store = mutableMapOf<String, RefreshToken>()

        fun contains(tokenId: String): Boolean = store.containsKey(tokenId)

        override fun save(refreshToken: RefreshToken): RefreshToken {
            store[refreshToken.tokenId] = refreshToken
            return refreshToken
        }

        override fun deleteByTokenId(tokenId: String): Long = if (store.remove(tokenId) != null) 1L else 0L
    }
}
