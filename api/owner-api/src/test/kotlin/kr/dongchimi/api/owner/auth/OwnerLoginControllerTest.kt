package kr.dongchimi.api.owner.auth

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import kr.dongchimi.api.core.auth.RefreshTokenCookieFactory
import kr.dongchimi.api.core.auth.RefreshTokenCookieProperties
import kr.dongchimi.api.owner.auth.request.OwnerLoginRequest
import kr.dongchimi.api.owner.auth.response.OwnerLoginResponse
import kr.dongchimi.core.owner.OwnerLoginCommand
import org.mockito.Mockito
import org.springframework.http.HttpHeaders
import org.springframework.mock.web.MockHttpServletResponse
import java.time.LocalDateTime

private fun cookieFactory() =
    RefreshTokenCookieFactory(
        RefreshTokenCookieProperties(name = "refresh_token", path = "/v1/auth/token/refresh", sameSite = "Lax", secure = true),
    )

private fun loginResponse(
    marketId: Long? = null,
    marketName: String? = null,
    marketThumbnailUrl: String? = null,
) = OwnerLoginResponse(
    accessToken = "access-token",
    ownerId = 1L,
    email = "owner@dongchimi.kr",
    marketId = marketId,
    marketName = marketName,
    marketThumbnailUrl = marketThumbnailUrl,
)

class OwnerLoginControllerTest :
    FunSpec({
        test("isAutoLogin=true 로그인 시 facade 응답을 그대로 내려주고 refresh는 영속 쿠키로 내려준다") {
            val facade = Mockito.mock(OwnerLoginQueryFacade::class.java)
            val command = OwnerLoginCommand("owner@dongchimi.kr", "password123!", isAutoLogin = true)
            Mockito
                .`when`(facade.login(command))
                .thenReturn(
                    OwnerLoginResult(
                        response = loginResponse(marketId = 10L, marketName = "신선마트"),
                        refreshToken = "refresh-token",
                        refreshExpiresAt = LocalDateTime.now().plusDays(14),
                        isAutoLogin = true,
                    ),
                )
            val controller = OwnerLoginController(facade, cookieFactory())
            val response = MockHttpServletResponse()

            val result =
                controller.login(OwnerLoginRequest("owner@dongchimi.kr", "password123!", isAutoLogin = true), response)

            result.data?.accessToken shouldBe "access-token"
            result.data?.ownerId shouldBe 1L
            result.data?.marketId shouldBe 10L
            result.data?.marketName shouldBe "신선마트"

            val setCookie = response.getHeader(HttpHeaders.SET_COOKIE)!!
            setCookie.shouldContain("refresh_token=refresh-token")
            setCookie.shouldContain("HttpOnly")
            setCookie.shouldContain("Max-Age=") // 영속 쿠키
        }

        test("isAutoLogin=false 로그인 시 refresh는 세션 쿠키(Max-Age 없음)로 내려준다") {
            val facade = Mockito.mock(OwnerLoginQueryFacade::class.java)
            val command = OwnerLoginCommand("owner@dongchimi.kr", "password123!", isAutoLogin = false)
            Mockito
                .`when`(facade.login(command))
                .thenReturn(
                    OwnerLoginResult(
                        response = loginResponse(),
                        refreshToken = "refresh-token",
                        refreshExpiresAt = LocalDateTime.now().plusDays(14),
                        isAutoLogin = false,
                    ),
                )
            val controller = OwnerLoginController(facade, cookieFactory())
            val response = MockHttpServletResponse()

            val result =
                controller.login(OwnerLoginRequest("owner@dongchimi.kr", "password123!", isAutoLogin = false), response)

            result.data?.marketId shouldBe null

            val setCookie = response.getHeader(HttpHeaders.SET_COOKIE)!!
            setCookie.shouldContain("refresh_token=refresh-token")
            setCookie.shouldNotContain("Max-Age") // 세션 쿠키
        }
    })
