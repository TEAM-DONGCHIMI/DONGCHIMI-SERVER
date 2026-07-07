package kr.dongchimi.core.auth

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kr.dongchimi.core.common.exception.CoreException
import java.time.LocalDateTime

class ReissueTokenServiceTest :
    FunSpec({
        test("유효한 refresh token이면 같은 userId·roles로 새 토큰을 재발급하고 회전한다") {
            val repository = FakeRefreshTokenRepository()
            repository.save(RefreshToken(tokenId = "old-id", userId = 7L, expiresAt = LocalDateTime.now().plusDays(14)))
            val tokenProvider = FakeTokenProvider(userId = 7L, roles = setOf(Role.OWNER.name), oldTokenId = "old-id")
            val service = ReissueTokenService(tokenProvider, RefreshTokenRotator(repository))

            val reissued = service.reissue("old-refresh-token-value")

            reissued.accessToken shouldBe "access-7-[OWNER]"
            reissued.refreshToken shouldBe "refresh-7-[OWNER]"
            repository.contains("old-id") shouldBe false
            repository.contains("new-token-id") shouldBe true
        }

        test("유효하지 않은 refresh token이면 예외가 발생한다") {
            val repository = FakeRefreshTokenRepository()
            val tokenProvider = FakeTokenProvider(parseShouldFail = true)
            val service = ReissueTokenService(tokenProvider, RefreshTokenRotator(repository))

            shouldThrow<CoreException> {
                service.reissue("invalid-value")
            }.errorCode shouldBe AuthErrorCode.INVALID_REFRESH_TOKEN
        }

        test("이미 폐기(회전됨/재사용)된 refresh token이면 예외가 발생한다") {
            val repository = FakeRefreshTokenRepository()
            val tokenProvider = FakeTokenProvider(userId = 7L, roles = setOf(Role.USER.name), oldTokenId = "already-rotated-id")
            val service = ReissueTokenService(tokenProvider, RefreshTokenRotator(repository))

            shouldThrow<CoreException> {
                service.reissue("reused-refresh-token-value")
            }.errorCode shouldBe AuthErrorCode.INVALID_REFRESH_TOKEN
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

    private class FakeTokenProvider(
        private val userId: Long = 0L,
        private val roles: Set<String> = emptySet(),
        private val oldTokenId: String = "",
        private val parseShouldFail: Boolean = false,
    ) : TokenProvider {
        override fun issueAccessToken(
            userId: Long,
            roles: Set<String>,
        ): String = "access-$userId-$roles"

        override fun issueRefreshToken(
            userId: Long,
            roles: Set<String>,
        ): IssuedRefreshToken = IssuedRefreshToken("refresh-$userId-$roles", "new-token-id", LocalDateTime.now().plusDays(14))

        override fun parseRefreshToken(tokenValue: String): RefreshTokenPayload {
            if (parseShouldFail) throw CoreException(AuthErrorCode.INVALID_REFRESH_TOKEN)
            return RefreshTokenPayload(oldTokenId, userId, roles)
        }
    }
}
