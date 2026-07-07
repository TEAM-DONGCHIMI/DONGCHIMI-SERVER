package kr.dongchimi.api.owner.auth

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import kr.dongchimi.api.core.auth.RefreshTokenCookieFactory
import kr.dongchimi.api.core.auth.RefreshTokenCookieProperties
import kr.dongchimi.api.owner.auth.request.OwnerLoginRequest
import kr.dongchimi.core.auth.AuthTokens
import kr.dongchimi.core.market.LocationPoint
import kr.dongchimi.core.market.Market
import kr.dongchimi.core.market.MarketPhoneNumber
import kr.dongchimi.core.owner.Owner
import kr.dongchimi.core.owner.OwnerLoginCommand
import kr.dongchimi.core.owner.OwnerLoginResult
import kr.dongchimi.core.owner.OwnerLoginService
import org.mockito.Mockito
import org.springframework.http.HttpHeaders
import org.springframework.mock.web.MockHttpServletResponse
import java.time.LocalDateTime

private fun cookieFactory() =
    RefreshTokenCookieFactory(
        RefreshTokenCookieProperties(name = "refresh_token", path = "/v1/auth/token/refresh", sameSite = "Lax", secure = true),
    )

private fun sampleMarket() =
    Market(
        id = 10L,
        ownerId = 1L,
        name = "신선마트",
        address = "서울시 어딘가",
        thumbnailUrl = "https://cdn.example.com/market/10.png",
        location = LocationPoint(127.0, 37.0),
        businessHours = null,
        phoneNumber = MarketPhoneNumber("02-000-0000", null, 1, "010-0000-0000", null, 1),
        brn = null,
    )

class OwnerLoginControllerTest :
    FunSpec({
        test("isAutoLogin=true 로그인 시 마트 정보와 함께 응답하고 refresh는 영속 쿠키로 내려준다") {
            val service = Mockito.mock(OwnerLoginService::class.java)
            val tokens = AuthTokens("access-token", "refresh-token", LocalDateTime.now().plusDays(14))
            val owner = Owner(id = 1L, email = "owner@dongchimi.kr", password = "encoded")
            Mockito
                .`when`(service.login(OwnerLoginCommand("owner@dongchimi.kr", "password123!", isAutoLogin = true)))
                .thenReturn(OwnerLoginResult(tokens, owner, sampleMarket(), isAutoLogin = true))
            val controller = OwnerLoginController(service, cookieFactory())
            val response = MockHttpServletResponse()

            val result =
                controller.login(OwnerLoginRequest("owner@dongchimi.kr", "password123!", isAutoLogin = true), response)

            result.data?.accessToken shouldBe "access-token"
            result.data?.ownerId shouldBe 1L
            result.data?.email shouldBe "owner@dongchimi.kr"
            result.data?.marketId shouldBe 10L
            result.data?.marketName shouldBe "신선마트"

            val setCookie = response.getHeader(HttpHeaders.SET_COOKIE)!!
            setCookie.shouldContain("refresh_token=refresh-token")
            setCookie.shouldContain("HttpOnly")
            setCookie.shouldContain("Max-Age=") // 영속 쿠키
        }

        test("isAutoLogin=false 로그인 시 refresh는 세션 쿠키(Max-Age 없음)로 내려주고 마트 필드는 null이다") {
            val service = Mockito.mock(OwnerLoginService::class.java)
            val tokens = AuthTokens("access-token", "refresh-token", LocalDateTime.now().plusDays(14))
            val owner = Owner(id = 1L, email = "owner@dongchimi.kr", password = "encoded")
            Mockito
                .`when`(service.login(OwnerLoginCommand("owner@dongchimi.kr", "password123!", isAutoLogin = false)))
                .thenReturn(OwnerLoginResult(tokens, owner, market = null, isAutoLogin = false))
            val controller = OwnerLoginController(service, cookieFactory())
            val response = MockHttpServletResponse()

            val result =
                controller.login(OwnerLoginRequest("owner@dongchimi.kr", "password123!", isAutoLogin = false), response)

            result.data?.marketId shouldBe null
            result.data?.marketName shouldBe null
            result.data?.marketThumbnailUrl shouldBe null

            val setCookie = response.getHeader(HttpHeaders.SET_COOKIE)!!
            setCookie.shouldContain("refresh_token=refresh-token")
            setCookie.shouldNotContain("Max-Age") // 세션 쿠키
        }
    })
