package kr.dongchimi.api.user.market

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kr.dongchimi.api.core.common.dto.CursorSliceResponse
import kr.dongchimi.api.core.common.exception.InvalidInputException
import kr.dongchimi.api.user.UserApiUser
import kr.dongchimi.api.user.market.request.NearbyMarketSearchRequest
import kr.dongchimi.api.user.market.response.MarketDetailResponse
import kr.dongchimi.api.user.market.response.NearbyMarketResponse
import kr.dongchimi.core.market.LocationPoint
import kr.dongchimi.core.market.NearbyMarketSearchCondition
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import java.time.LocalDateTime

class UserMarketControllerTest :
    FunSpec({
        val apiUser = UserApiUser(userId = 1L, roles = setOf("USER"))

        test("상세 조회 성공 시 slug로 facade를 호출하고 응답을 반환한다") {
            val facade = Mockito.mock(MarketDetailQueryFacade::class.java)
            val detail = sampleDetail()
            Mockito
                .`when`(facade.getDetail(eqString("market-slug"), anyLocalDateTime()))
                .thenReturn(detail)
            val controller = UserMarketController(facade, Mockito.mock(NearbyMarketQueryFacade::class.java))

            val response = controller.getDetail(apiUser, "market-slug")

            response.success shouldBe true
            response.data shouldBe detail
        }

        test("위치 기준 목록 조회 성공 시 검증된 조건으로 facade를 호출하고 응답을 반환한다") {
            val facade = Mockito.mock(NearbyMarketQueryFacade::class.java)
            val list = CursorSliceResponse<NearbyMarketResponse>(content = emptyList(), hasNext = false, nextCursor = null)
            Mockito
                .`when`(facade.getNearbyMarkets(anyCondition(), anyLocalDateTime()))
                .thenReturn(list)
            val controller = UserMarketController(Mockito.mock(MarketDetailQueryFacade::class.java), facade)

            val response = controller.getNearbyMarkets(apiUser, NearbyMarketSearchRequest(lat = 37.55, lng = 126.98))

            response.success shouldBe true
            response.data shouldBe list
        }

        test("위도가 유효 범위를 벗어나면 facade를 호출하지 않고 예외를 던진다") {
            val facade = Mockito.mock(NearbyMarketQueryFacade::class.java)
            val controller = UserMarketController(Mockito.mock(MarketDetailQueryFacade::class.java), facade)

            shouldThrow<InvalidInputException> {
                controller.getNearbyMarkets(apiUser, NearbyMarketSearchRequest(lat = 91.0, lng = 126.98))
            }

            Mockito.verifyNoInteractions(facade)
        }
    })

// Kotlin 비널 파라미터에 Mockito 매처를 쓰기 위한 헬퍼 (매처는 null을 반환하므로 폴백을 준다)
private fun anyLocalDateTime(): LocalDateTime = Mockito.any(LocalDateTime::class.java) ?: LocalDateTime.now()

private fun eqString(value: String): String = ArgumentMatchers.eq(value) ?: value

private fun anyCondition(): NearbyMarketSearchCondition =
    Mockito.any(NearbyMarketSearchCondition::class.java)
        ?: NearbyMarketSearchCondition(LocationPoint(0.0, 0.0), 1000.0, null, 5)

private fun sampleDetail() =
    MarketDetailResponse(
        marketId = 10L,
        name = "망원 신선마트",
        thumbnailUrl = null,
        address = "서울시 마포구 망원동",
        isOpenNow = true,
        businessHours = emptyList(),
        marketPhone1 = "02-123-4567",
        marketPhone2 = null,
        ownerPhone = "010-1234-5678",
        top3 = emptyList(),
    )
