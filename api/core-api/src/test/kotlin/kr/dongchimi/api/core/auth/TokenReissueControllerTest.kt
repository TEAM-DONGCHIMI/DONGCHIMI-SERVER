package kr.dongchimi.api.core.auth

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import kr.dongchimi.core.auth.AuthErrorCode
import kr.dongchimi.core.auth.AuthTokens
import kr.dongchimi.core.auth.ReissueTokenService
import kr.dongchimi.core.common.exception.CoreException
import org.mockito.Mockito
import org.springframework.http.HttpHeaders
import org.springframework.mock.web.MockHttpServletResponse
import java.time.LocalDateTime

class TokenReissueControllerTest :
    FunSpec({
        val properties =
            RefreshTokenCookieProperties(
                name = "refresh_token",
                path = "/v1/auth/token/refresh",
                sameSite = "Lax",
                secure = true,
            )
        val cookieFactory = RefreshTokenCookieFactory(properties)

        test("유효한 refresh token 쿠키면 재발급하고 새 refresh token을 쿠키로 내려준다") {
            val reissueTokenService = Mockito.mock(ReissueTokenService::class.java)
            Mockito
                .`when`(reissueTokenService.reissue("old-refresh-value"))
                .thenReturn(
                    AuthTokens(
                        accessToken = "new-access",
                        refreshToken = "new-refresh",
                        refreshExpiresAt = LocalDateTime.now().plusDays(14),
                    ),
                )
            val controller = TokenReissueController(reissueTokenService, cookieFactory)
            val response = MockHttpServletResponse()

            val result = controller.reissue("old-refresh-value", response)

            result.data?.accessToken shouldBe "new-access"
            val setCookie = response.getHeader(HttpHeaders.SET_COOKIE)
            setCookie.shouldContain("refresh_token=new-refresh")
            setCookie.shouldContain("HttpOnly")
            setCookie.shouldContain("Secure")
            setCookie.shouldContain("SameSite=Lax")
            setCookie.shouldContain("Path=/v1/auth/token/refresh")
        }

        test("refresh token 쿠키가 없으면 예외가 발생한다") {
            val reissueTokenService = Mockito.mock(ReissueTokenService::class.java)
            val controller = TokenReissueController(reissueTokenService, cookieFactory)

            val exception =
                shouldThrow<CoreException> {
                    controller.reissue(null, MockHttpServletResponse())
                }

            exception.errorCode shouldBe AuthErrorCode.MISSING_REFRESH_TOKEN
        }
    })
