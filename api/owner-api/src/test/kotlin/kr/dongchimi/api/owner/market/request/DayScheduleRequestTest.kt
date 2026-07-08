package kr.dongchimi.api.owner.market.request

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kr.dongchimi.core.common.exception.CommonErrorCode
import kr.dongchimi.core.common.exception.CoreException
import kr.dongchimi.core.market.DaySchedule
import java.time.LocalTime

class DayScheduleRequestTest :
    FunSpec({
        test("open/close가 모두 있으면 DaySchedule로 변환한다") {
            val daySchedule = DayScheduleRequest(open = "09:00", close = "18:00").toDaySchedule()

            daySchedule shouldBe DaySchedule(openTime = LocalTime.of(9, 0), closeTime = LocalTime.of(18, 0))
        }

        test("open이 없으면 예외가 발생한다") {
            val exception = shouldThrow<CoreException> { DayScheduleRequest(open = null, close = "18:00").toDaySchedule() }

            exception.errorCode shouldBe CommonErrorCode.INVALID_INPUT
        }

        test("close가 공백이면 예외가 발생한다") {
            val exception = shouldThrow<CoreException> { DayScheduleRequest(open = "09:00", close = " ").toDaySchedule() }

            exception.errorCode shouldBe CommonErrorCode.INVALID_INPUT
        }

        test("HH:mm 형식이 아니면 예외가 발생한다") {
            val exception = shouldThrow<CoreException> { DayScheduleRequest(open = "9시", close = "18:00").toDaySchedule() }

            exception.errorCode shouldBe CommonErrorCode.INVALID_INPUT
        }
    })
