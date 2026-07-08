package kr.dongchimi.api.owner.market.request

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kr.dongchimi.core.common.exception.CommonErrorCode
import kr.dongchimi.core.common.exception.CoreException
import kr.dongchimi.core.market.LocationPoint
import kr.dongchimi.core.market.MarketInfo
import kr.dongchimi.core.market.MarketPhoneNumber
import kr.dongchimi.core.market.MarketUpdateCommand

class MarketUpdateRequestTest :
    FunSpec({
        test("정상 입력이면 MarketUpdateCommand로 변환하고 address와 detailAddress를 합친다") {
            val command = sampleRequest(address = "서울특별시 성북구", detailAddress = "모던하우스 123호").toCommand()

            command shouldBe
                MarketUpdateCommand(
                    info =
                        MarketInfo(
                            name = "동치미 마트 강남점",
                            address = "서울특별시 성북구|모던하우스 123호",
                            thumbnailUrl = "https://static.dongchimi.kr/market.png",
                        ),
                    location = LocationPoint(longitude = 127.0, latitude = 37.0),
                    businessHours = sampleBusinessHoursRequest().toBusinessHours(),
                    phoneNumber = MarketPhoneNumber("02-000-0000", null, 1, "010-0000-0000"),
                    brn = "000-00-00000",
                )
        }

        test("주소가 공백이면 예외가 발생한다") {
            val exception = shouldThrow<CoreException> { sampleRequest(address = " ").toCommand() }

            exception.errorCode shouldBe CommonErrorCode.INVALID_INPUT
        }

        test("마트명이 15자를 초과하면 예외가 발생한다") {
            val exception = shouldThrow<CoreException> { sampleRequest(name = "a".repeat(16)).toCommand() }

            exception.errorCode shouldBe CommonErrorCode.INVALID_INPUT
        }

        test("상세 주소가 없으면 예외가 발생한다") {
            val exception = shouldThrow<CoreException> { sampleRequest(detailAddress = null).toCommand() }

            exception.errorCode shouldBe CommonErrorCode.INVALID_INPUT
        }

        test("상세 주소가 20자를 초과하면 예외가 발생한다") {
            val exception = shouldThrow<CoreException> { sampleRequest(detailAddress = "a".repeat(21)).toCommand() }

            exception.errorCode shouldBe CommonErrorCode.INVALID_INPUT
        }
    })

private fun sampleBusinessHoursRequest(): BusinessHoursRequest =
    BusinessHoursRequest(mon = null, tue = null, wed = null, thu = null, fri = null, sat = null, sun = null)

private fun sampleRequest(
    name: String = "동치미 마트 강남점",
    thumbnailUrl: String? = "https://static.dongchimi.kr/market.png",
    address: String = "서울특별시 성북구 정릉동 880-20",
    detailAddress: String? = "모던하우스 123호",
    latitude: Double = 37.0,
    longitude: Double = 127.0,
    marketPhone1: String = "02-000-0000",
    marketPhone2: String? = null,
    marketPhonePrimary: Short = 1,
    ownerPhone: String = "010-0000-0000",
    brn: String? = "000-00-00000",
): MarketUpdateRequest =
    MarketUpdateRequest(
        name = name,
        thumbnailUrl = thumbnailUrl,
        address = address,
        detailAddress = detailAddress,
        latitude = latitude,
        longitude = longitude,
        businessHours = sampleBusinessHoursRequest(),
        marketPhone1 = marketPhone1,
        marketPhone2 = marketPhone2,
        marketPhonePrimary = marketPhonePrimary,
        ownerPhone = ownerPhone,
        brn = brn,
    )
