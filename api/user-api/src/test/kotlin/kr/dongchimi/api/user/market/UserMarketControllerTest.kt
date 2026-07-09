package kr.dongchimi.api.user.market

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kr.dongchimi.api.user.UserApiUser
import kr.dongchimi.api.user.market.response.MarketDetailResponse
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
            val controller = UserMarketController(facade)

            val response = controller.getDetail(apiUser, "market-slug")

            response.success shouldBe true
            response.data shouldBe detail
        }
    })

// Kotlin 비널 파라미터에 Mockito 매처를 쓰기 위한 헬퍼 (매처는 null을 반환하므로 폴백을 준다)
private fun anyLocalDateTime(): LocalDateTime = Mockito.any(LocalDateTime::class.java) ?: LocalDateTime.now()

private fun eqString(value: String): String = ArgumentMatchers.eq(value) ?: value

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
