package kr.dongchimi.api.owner.flyer

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kr.dongchimi.api.owner.OwnerApiUser
import kr.dongchimi.api.owner.flyer.response.FlyerPreviewBusinessHourResponse
import kr.dongchimi.api.owner.flyer.response.FlyerPreviewDailyResponse
import kr.dongchimi.api.owner.flyer.response.FlyerPreviewResponse
import kr.dongchimi.core.market.FlyerService
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import java.time.LocalDateTime

class FlyerControllerTest :
    FunSpec({
        val apiUser = OwnerApiUser(userId = 1L, roles = setOf("OWNER"))
        val marketId = 10L

        test("기간 할인 전단 미리보기 조회 시 apiUser의 userId로 Facade를 호출하고 성공 응답을 반환한다") {
            val flyerService = Mockito.mock(FlyerService::class.java)
            val flyerPreviewQueryFacade = Mockito.mock(FlyerPreviewQueryFacade::class.java)
            val response = sampleResponse()
            Mockito
                .`when`(
                    flyerPreviewQueryFacade.getPeriodicPreview(
                        ArgumentMatchers.eq(apiUser.userId),
                        ArgumentMatchers.eq(marketId),
                        anyLocalDateTime(),
                    ),
                ).thenReturn(response)

            val controller = FlyerController(flyerService, flyerPreviewQueryFacade)

            val result = controller.getPeriodicPreview(apiUser, marketId)

            result.success shouldBe true
            result.data shouldBe response
        }
    })

// Kotlin 비널 파라미터에 Mockito 매처를 쓰기 위한 헬퍼 (매처는 null을 반환하므로 폴백을 준다)
private fun anyLocalDateTime(): LocalDateTime = Mockito.any(LocalDateTime::class.java) ?: LocalDateTime.now()

private fun sampleResponse() =
    FlyerPreviewResponse(
        marketId = 10L,
        name = "망원 신선마트",
        thumbnailUrl = null,
        address = "서울시 마포구 망원동",
        isOpenNow = true,
        businessHours = listOf(FlyerPreviewBusinessHourResponse(days = listOf("MONDAY"), isOpen = true, open = "10:00", close = "20:00")),
        marketPhone1 = "02-123-4567",
        marketPhone2 = null,
        ownerPhone = "010-1234-5678",
        top3 = emptyList(),
        daily = FlyerPreviewDailyResponse(totalCount = 0, products = emptyList()),
        preparedProducts = emptyList(),
    )
