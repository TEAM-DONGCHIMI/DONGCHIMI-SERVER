package kr.dongchimi.api.owner.market

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kr.dongchimi.api.owner.OwnerApiUser
import kr.dongchimi.api.owner.market.request.BusinessHoursRequest
import kr.dongchimi.api.owner.market.request.MarketRegisterRequest
import kr.dongchimi.api.owner.market.request.MarketUpdateRequest
import kr.dongchimi.core.market.LocationPoint
import kr.dongchimi.core.market.Market
import kr.dongchimi.core.market.MarketInfo
import kr.dongchimi.core.market.MarketPhoneNumber
import kr.dongchimi.core.market.MarketService
import org.mockito.Mockito

class MarketControllerTest :
    FunSpec({
        val apiUser = OwnerApiUser(userId = 1L, roles = setOf("OWNER"))
        val businessHours = BusinessHoursRequest(mon = null, tue = null, wed = null, thu = null, fri = null, sat = null, sun = null)

        test("등록 성공 시 apiUser의 userId로 서비스를 호출하고 성공 응답을 반환한다") {
            val marketService = Mockito.mock(MarketService::class.java)
            val request =
                MarketRegisterRequest(
                    name = "동치미 마트 강남점",
                    thumbnailUrl = null,
                    address = "서울특별시 성북구",
                    detailAddress = "123호",
                    latitude = 37.0,
                    longitude = 127.0,
                    businessHours = businessHours,
                    marketPhone1 = "02-000-0000",
                    marketPhone2 = null,
                    marketPhonePrimary = 1,
                    ownerPhone = "010-0000-0000",
                    brn = null,
                )
            val savedMarket = sampleMarket()
            Mockito.`when`(marketService.register(1L, request.toCommand())).thenReturn(savedMarket)
            val controller = MarketController(marketService)

            val response = controller.register(apiUser, request)

            response.success shouldBe true
            Mockito.verify(marketService).register(1L, request.toCommand())
        }

        test("수정 성공 시 apiUser의 userId와 marketId로 서비스를 호출하고 성공 응답을 반환한다") {
            val marketService = Mockito.mock(MarketService::class.java)
            val request =
                MarketUpdateRequest(
                    name = "동치미 마트 강남점",
                    thumbnailUrl = null,
                    address = "서울특별시 성북구",
                    detailAddress = "123호",
                    latitude = 37.0,
                    longitude = 127.0,
                    businessHours = businessHours,
                    marketPhone1 = "02-000-0000",
                    marketPhone2 = null,
                    marketPhonePrimary = 1,
                    ownerPhone = "010-0000-0000",
                    brn = null,
                )
            val updatedMarket = sampleMarket()
            Mockito.`when`(marketService.update(1L, 10L, request.toCommand())).thenReturn(updatedMarket)
            val controller = MarketController(marketService)

            val response = controller.update(apiUser, 10L, request)

            response.success shouldBe true
            Mockito.verify(marketService).update(1L, 10L, request.toCommand())
        }
    })

private fun sampleMarket(): Market =
    Market(
        id = 10L,
        ownerId = 1L,
        info = MarketInfo(name = "동치미 마트 강남점", address = "서울특별시 성북구", thumbnailUrl = null),
        location = LocationPoint(longitude = 127.0, latitude = 37.0),
        businessHours = null,
        phoneNumber = MarketPhoneNumber("02-000-0000", null, 1, "010-0000-0000"),
        brn = null,
    )
