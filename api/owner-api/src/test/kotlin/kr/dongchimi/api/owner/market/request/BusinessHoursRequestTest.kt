package kr.dongchimi.api.owner.market.request

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kr.dongchimi.core.market.DaySchedule
import java.time.LocalTime

class BusinessHoursRequestTest :
    FunSpec({
        test("휴무일은 null, 영업일은 DaySchedule로 매핑한다") {
            val request =
                BusinessHoursRequest(
                    mon = DayScheduleRequest("09:00", "18:00"),
                    tue = DayScheduleRequest("09:00", "18:00"),
                    wed = DayScheduleRequest("09:00", "18:00"),
                    thu = DayScheduleRequest("09:00", "18:00"),
                    fri = DayScheduleRequest("09:00", "18:00"),
                    sat = null,
                    sun = null,
                )

            val businessHours = request.toBusinessHours()

            businessHours.monday shouldBe DaySchedule(LocalTime.of(9, 0), LocalTime.of(18, 0))
            businessHours.saturday shouldBe null
            businessHours.sunday shouldBe null
        }
    })
