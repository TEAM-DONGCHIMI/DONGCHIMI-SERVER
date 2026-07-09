package kr.dongchimi.api.owner.market.request

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kr.dongchimi.core.common.exception.CommonErrorCode
import kr.dongchimi.core.common.exception.CoreException
import kr.dongchimi.core.market.LocationPoint
import kr.dongchimi.core.market.MarketInfo
import kr.dongchimi.core.market.MarketPhoneNumber
import kr.dongchimi.core.market.MarketRegisterCommand

class MarketRegisterRequestTest :
    FunSpec({
        test("정상 입력이면 MarketRegisterCommand로 변환하고 address와 detailAddress를 합친다") {
            val command = sampleRequest(address = "서울특별시 성북구", detailAddress = "모던하우스 123호").toCommand()

            command shouldBe
                MarketRegisterCommand(
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

        test("마트명이 공백이면 예외가 발생한다") {
            val exception = shouldThrow<CoreException> { sampleRequest(name = " ").toCommand() }

            exception.errorCode shouldBe CommonErrorCode.INVALID_INPUT
        }

        test("마트명이 15자를 초과하면 예외가 발생한다") {
            val exception = shouldThrow<CoreException> { sampleRequest(name = "a".repeat(16)).toCommand() }

            exception.errorCode shouldBe CommonErrorCode.INVALID_INPUT
        }

        test("마트명이 15자이면 통과한다") {
            val command = sampleRequest(name = "a".repeat(15)).toCommand()

            command.info.name shouldBe "a".repeat(15)
        }

        test("주소가 공백이면 예외가 발생한다") {
            val exception = shouldThrow<CoreException> { sampleRequest(address = " ").toCommand() }

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

        test("상세 주소가 20자이면 통과한다") {
            val command = sampleRequest(detailAddress = "a".repeat(20)).toCommand()

            command.info.address shouldBe "${"서울특별시 성북구 정릉동 880-20"}|${"a".repeat(20)}"
        }

        test("위도가 범위를 벗어나면 예외가 발생한다") {
            val exception = shouldThrow<CoreException> { sampleRequest(latitude = 91.0).toCommand() }

            exception.errorCode shouldBe CommonErrorCode.INVALID_INPUT
        }

        test("경도가 범위를 벗어나면 예외가 발생한다") {
            val exception = shouldThrow<CoreException> { sampleRequest(longitude = 181.0).toCommand() }

            exception.errorCode shouldBe CommonErrorCode.INVALID_INPUT
        }

        test("marketPhonePrimary가 1 또는 2가 아니면 예외가 발생한다") {
            val exception = shouldThrow<CoreException> { sampleRequest(marketPhonePrimary = 3).toCommand() }

            exception.errorCode shouldBe CommonErrorCode.INVALID_INPUT
        }

        test("marketPhonePrimary가 2인데 marketPhone2가 없으면 예외가 발생한다") {
            val exception =
                shouldThrow<CoreException> {
                    sampleRequest(marketPhonePrimary = 2, marketPhone2 = null).toCommand()
                }

            exception.errorCode shouldBe CommonErrorCode.INVALID_INPUT
        }

        test("brn 형식이 올바르지 않으면 예외가 발생한다") {
            val exception = shouldThrow<CoreException> { sampleRequest(brn = "12345").toCommand() }

            exception.errorCode shouldBe CommonErrorCode.INVALID_INPUT
        }

        test("brn이 없으면 그대로 통과한다") {
            val command = sampleRequest(brn = null).toCommand()

            command.brn shouldBe null
        }
    })

private fun sampleBusinessHoursRequest(): List<BusinessHourSlotRequest> =
    listOf(
        BusinessHourSlotRequest(days = listOf("MONDAY", "TUESDAY"), isOpen = true, open = "10:00", close = "20:00"),
        BusinessHourSlotRequest(days = listOf("SUNDAY"), isOpen = false, open = null, close = null),
    )

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
): MarketRegisterRequest =
    MarketRegisterRequest(
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
