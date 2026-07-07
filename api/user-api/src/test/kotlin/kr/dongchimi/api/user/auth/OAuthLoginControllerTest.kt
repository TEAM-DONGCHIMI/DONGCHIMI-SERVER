package kr.dongchimi.api.user.auth

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import kr.dongchimi.api.core.auth.RefreshTokenCookieFactory
import kr.dongchimi.api.core.auth.RefreshTokenCookieProperties
import kr.dongchimi.api.user.auth.request.OAuthLoginRequest
import kr.dongchimi.core.auth.AuthTokens
import kr.dongchimi.core.auth.OAuthLoginCommand
import kr.dongchimi.core.auth.OAuthLoginService
import kr.dongchimi.core.user.SocialProvider
import org.mockito.Mockito
import org.springframework.http.HttpHeaders
import org.springframework.mock.web.MockHttpServletResponse
import java.time.LocalDateTime

class OAuthLoginControllerTest :
    FunSpec({
        test("로그인 성공 시 accessToken은 body로, refreshToken은 Set-Cookie로 내려준다") {
            val oAuthLoginService = Mockito.mock(OAuthLoginService::class.java)
            Mockito
                .`when`(oAuthLoginService.login(OAuthLoginCommand(SocialProvider.KAKAO, "kakao-token")))
                .thenReturn(
                    AuthTokens(
                        accessToken = "access-token",
                        refreshToken = "refresh-token",
                        refreshExpiresAt = LocalDateTime.now().plusDays(14),
                    ),
                )
            val cookieFactory =
                RefreshTokenCookieFactory(
                    RefreshTokenCookieProperties(
                        name = "refresh_token",
                        path = "/v1/auth/token/refresh",
                        sameSite = "Lax",
                        secure = true,
                    ),
                )
            val controller = OAuthLoginController(oAuthLoginService, cookieFactory)
            val response = MockHttpServletResponse()

            val result = controller.login("kakao", OAuthLoginRequest("kakao-token"), response)

            result.data?.accessToken shouldBe "access-token"
            val setCookie = response.getHeader(HttpHeaders.SET_COOKIE)
            setCookie.shouldContain("refresh_token=refresh-token")
            setCookie.shouldContain("HttpOnly")
            setCookie.shouldContain("Secure")
            setCookie.shouldContain("SameSite=Lax")
            setCookie.shouldContain("Path=/v1/auth/token/refresh")
        }
    })
