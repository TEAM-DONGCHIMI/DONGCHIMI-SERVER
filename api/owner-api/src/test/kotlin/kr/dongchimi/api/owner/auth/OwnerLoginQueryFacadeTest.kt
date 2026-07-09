package kr.dongchimi.api.owner.auth

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kr.dongchimi.core.auth.AuthTokens
import kr.dongchimi.core.market.BusinessHours
import kr.dongchimi.core.market.LocationPoint
import kr.dongchimi.core.market.Market
import kr.dongchimi.core.market.MarketInfo
import kr.dongchimi.core.market.MarketPhoneNumber
import kr.dongchimi.core.market.MarketService
import kr.dongchimi.core.owner.Owner
import kr.dongchimi.core.owner.OwnerAuthResult
import kr.dongchimi.core.owner.OwnerAuthService
import kr.dongchimi.core.owner.OwnerLoginCommand
import org.mockito.Mockito
import java.time.LocalDateTime

class OwnerLoginQueryFacadeTest :
    FunSpec({
        val command = OwnerLoginCommand("owner@dongchimi.kr", "password123!", isAutoLogin = true)
        val owner = Owner(id = 1L, email = "owner@dongchimi.kr", password = "encoded")
        val tokens = AuthTokens("access-token", "refresh-token", LocalDateTime.now().plusDays(14))

        test("마트를 등록한 점주면 로그인 응답에 마트 정보를 함께 조합한다") {
            val authService = Mockito.mock(OwnerAuthService::class.java)
            val marketService = Mockito.mock(MarketService::class.java)
            Mockito.`when`(authService.login(command)).thenReturn(OwnerAuthResult(tokens, owner, isAutoLogin = true))
            Mockito.`when`(marketService.findByOwnerId(1L)).thenReturn(market(10L))

            val facade = OwnerLoginQueryFacade(authService, marketService)

            val result = facade.login(command)

            result.response.accessToken shouldBe "access-token"
            result.response.ownerId shouldBe 1L
            result.response.email shouldBe "owner@dongchimi.kr"
            result.response.marketId shouldBe 10L
            result.response.marketName shouldBe "신선마트"
            result.response.marketThumbnailUrl shouldBe "https://cdn.example.com/market/10.png"
            result.refreshToken shouldBe "refresh-token"
            result.isAutoLogin shouldBe true
        }

        test("마트가 없는 점주면 로그인 응답의 마트 필드는 null이다") {
            val authService = Mockito.mock(OwnerAuthService::class.java)
            val marketService = Mockito.mock(MarketService::class.java)
            Mockito.`when`(authService.login(command)).thenReturn(OwnerAuthResult(tokens, owner, isAutoLogin = true))
            Mockito.`when`(marketService.findByOwnerId(1L)).thenReturn(null)

            val facade = OwnerLoginQueryFacade(authService, marketService)

            val result = facade.login(command)

            result.response.marketId shouldBe null
            result.response.marketName shouldBe null
            result.response.marketThumbnailUrl shouldBe null
        }
    })

private fun market(marketId: Long) =
    Market(
        id = marketId,
        ownerId = 1L,
        info = MarketInfo(name = "신선마트", address = "서울시 어딘가", thumbnailUrl = "https://cdn.example.com/market/10.png"),
        location = LocationPoint(longitude = 127.0, latitude = 37.0),
        businessHours = BusinessHours(emptyList()),
        phoneNumber =
            MarketPhoneNumber(
                marketPhone1 = "02-000-0000",
                marketPhone2 = null,
                marketPhonePrimary = 1,
                ownerPhone = "010-0000-0000",
            ),
        brn = null,
    )
