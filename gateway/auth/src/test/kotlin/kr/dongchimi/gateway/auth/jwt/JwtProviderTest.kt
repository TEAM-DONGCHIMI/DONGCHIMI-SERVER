package kr.dongchimi.gateway.auth.jwt

import io.jsonwebtoken.ExpiredJwtException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kr.dongchimi.gateway.auth.config.JwtProperties
import java.util.UUID

class JwtProviderTest :
    FunSpec({
        fun jwtProvider(refreshTokenExpiry: Long = 1_209_600_000) =
            JwtProvider(
                JwtProperties(
                    secretKey = "test-secret-key-must-be-long-enough-for-hs256-aaaaaaaa",
                    issuer = "dongchimi-test",
                    accessTokenExpiry = 3_600_000,
                    refreshTokenExpiry = refreshTokenExpiry,
                ),
            )

        test("refresh token을 생성 후 파싱하면 tokenId·userId·roles가 보존된다") {
            val provider = jwtProvider()
            val tokenId = UUID.randomUUID().toString()
            val roles = setOf("USER", "OWNER")

            val token = provider.generateRefreshToken(userId = 42L, roles = roles, tokenId = tokenId)
            val payload = provider.parseRefreshToken(token)

            payload.tokenId shouldBe tokenId
            payload.userId shouldBe 42L
            payload.roles shouldBe roles
        }

        test("만료된 refresh token을 파싱하면 예외가 발생한다") {
            val provider = jwtProvider(refreshTokenExpiry = -1_000)
            val token = provider.generateRefreshToken(1L, setOf("USER"), UUID.randomUUID().toString())

            shouldThrow<ExpiredJwtException> {
                provider.parseRefreshToken(token)
            }
        }
    })
