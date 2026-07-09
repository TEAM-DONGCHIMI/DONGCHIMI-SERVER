package kr.dongchimi.api.user.flyer

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kr.dongchimi.api.user.UserApiUser
import kr.dongchimi.core.market.FlyerService
import kr.dongchimi.core.market.FlyerShareInfo
import org.mockito.Mockito

class UserFlyerControllerTest :
    FunSpec({
        val apiUser = UserApiUser(userId = 1L, roles = setOf("USER"))

        test("조회 성공 시 마트명·slug·QR코드를 포함한 성공 응답을 반환한다") {
            val flyerService = Mockito.mock(FlyerService::class.java)
            val shareInfo =
                FlyerShareInfo(
                    marketId = 10L,
                    marketName = "동치미 마트 강남점",
                    slug = "gangnam-mart",
                    qrCode = "data:image/png;base64,already-issued",
                )
            Mockito.`when`(flyerService.getShareInfo(10L)).thenReturn(shareInfo)
            val controller = UserFlyerController(flyerService)

            val response = controller.getShareInfo(apiUser, 10L)

            response.success shouldBe true
            response.data?.marketId shouldBe 10L
            response.data?.marketName shouldBe "동치미 마트 강남점"
            response.data?.slug shouldBe "gangnam-mart"
            response.data?.qrCode shouldBe "data:image/png;base64,already-issued"
        }
    })
