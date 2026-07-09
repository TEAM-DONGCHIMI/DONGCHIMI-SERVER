package kr.dongchimi.core.market

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime

class BusinessHoursTest :
    FunSpec({
        val businessHours =
            BusinessHours(
                listOf(
                    BusinessHourSlot(
                        days = listOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY),
                        isOpen = true,
                        open = LocalTime.of(10, 0),
                        close = LocalTime.of(20, 0),
                    ),
                    BusinessHourSlot(days = listOf(DayOfWeek.SUNDAY), isOpen = false),
                ),
            )

        // 2026-07-06(월), 07-07(화), 07-08(수), 07-12(일)
        test("영업일이고 영업시간 안이면 true") {
            businessHours.isOpenAt(LocalDateTime.of(2026, 7, 6, 14, 0)) shouldBe true
        }

        test("open 시각과 같으면 true (하한 포함)") {
            businessHours.isOpenAt(LocalDateTime.of(2026, 7, 6, 10, 0)) shouldBe true
        }

        test("close 시각과 같으면 false (상한 미포함)") {
            businessHours.isOpenAt(LocalDateTime.of(2026, 7, 6, 20, 0)) shouldBe false
        }

        test("영업시간 전이면 false") {
            businessHours.isOpenAt(LocalDateTime.of(2026, 7, 7, 9, 59)) shouldBe false
        }

        test("휴무일이면 false") {
            businessHours.isOpenAt(LocalDateTime.of(2026, 7, 12, 14, 0)) shouldBe false
        }

        test("영업시간 슬롯에 없는 요일이면 false") {
            businessHours.isOpenAt(LocalDateTime.of(2026, 7, 8, 14, 0)) shouldBe false
        }
    })
