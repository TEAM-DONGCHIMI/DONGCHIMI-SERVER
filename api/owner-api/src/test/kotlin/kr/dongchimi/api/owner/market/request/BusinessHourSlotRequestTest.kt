package kr.dongchimi.api.owner.market.request

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kr.dongchimi.core.common.exception.CommonErrorCode
import kr.dongchimi.core.common.exception.CoreException
import java.time.DayOfWeek
import java.time.LocalTime

class BusinessHourSlotRequestTest :
    FunSpec({
        test("정상 입력이면 BusinessHours로 변환한다") {
            val request =
                listOf(
                    BusinessHourSlotRequest(days = listOf("monday", "TUESDAY"), isOpen = true, open = "10:00", close = "20:00"),
                    BusinessHourSlotRequest(days = listOf("SUNDAY"), isOpen = false, open = null, close = null),
                )

            val businessHours = request.toBusinessHours()

            businessHours.slots.size shouldBe 2
            businessHours.slots[0].days shouldBe listOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY)
            businessHours.slots[0].open shouldBe LocalTime.of(10, 0)
            businessHours.slots[1].isOpen shouldBe false
        }

        test("비어 있으면 예외가 발생한다") {
            val exception = shouldThrow<CoreException> { emptyList<BusinessHourSlotRequest>().toBusinessHours() }

            exception.errorCode shouldBe CommonErrorCode.INVALID_INPUT
        }

        test("요일이 없으면 예외가 발생한다") {
            val request = listOf(BusinessHourSlotRequest(days = emptyList(), isOpen = true, open = "10:00", close = "20:00"))

            val exception = shouldThrow<CoreException> { request.toBusinessHours() }

            exception.errorCode shouldBe CommonErrorCode.INVALID_INPUT
        }

        test("요일 값이 올바르지 않으면 예외가 발생한다") {
            val request = listOf(BusinessHourSlotRequest(days = listOf("FUNDAY"), isOpen = true, open = "10:00", close = "20:00"))

            val exception = shouldThrow<CoreException> { request.toBusinessHours() }

            exception.errorCode shouldBe CommonErrorCode.INVALID_INPUT
        }

        test("여러 슬롯에 같은 요일이 중복되면 예외가 발생한다") {
            val request =
                listOf(
                    BusinessHourSlotRequest(days = listOf("MONDAY"), isOpen = true, open = "10:00", close = "20:00"),
                    BusinessHourSlotRequest(days = listOf("MONDAY"), isOpen = false, open = null, close = null),
                )

            val exception = shouldThrow<CoreException> { request.toBusinessHours() }

            exception.errorCode shouldBe CommonErrorCode.INVALID_INPUT
        }

        test("영업일인데 시간이 없으면 예외가 발생한다") {
            val request = listOf(BusinessHourSlotRequest(days = listOf("MONDAY"), isOpen = true, open = null, close = null))

            val exception = shouldThrow<CoreException> { request.toBusinessHours() }

            exception.errorCode shouldBe CommonErrorCode.INVALID_INPUT
        }

        test("마감 시각이 시작 시각보다 빠르면 예외가 발생한다") {
            val request = listOf(BusinessHourSlotRequest(days = listOf("MONDAY"), isOpen = true, open = "20:00", close = "10:00"))

            val exception = shouldThrow<CoreException> { request.toBusinessHours() }

            exception.errorCode shouldBe CommonErrorCode.INVALID_INPUT
        }

        test("시간 형식이 올바르지 않으면 예외가 발생한다") {
            val request = listOf(BusinessHourSlotRequest(days = listOf("MONDAY"), isOpen = true, open = "10시", close = "20:00"))

            val exception = shouldThrow<CoreException> { request.toBusinessHours() }

            exception.errorCode shouldBe CommonErrorCode.INVALID_INPUT
        }
    })
