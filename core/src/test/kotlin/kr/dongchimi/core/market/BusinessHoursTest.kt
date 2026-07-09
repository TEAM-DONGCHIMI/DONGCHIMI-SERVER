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

        context("자정을 넘기는 영업시간 (월요일 22:00 ~ 화요일 02:00)") {
            val overnight =
                BusinessHours(
                    listOf(
                        BusinessHourSlot(
                            days = listOf(DayOfWeek.MONDAY),
                            isOpen = true,
                            open = LocalTime.of(22, 0),
                            close = LocalTime.of(2, 0),
                        ),
                    ),
                )

            test("개점일 개점 시각 이후면 true") {
                overnight.isOpenAt(LocalDateTime.of(2026, 7, 6, 23, 0)) shouldBe true
            }

            test("자정을 넘긴 다음 날 마감 전이면 true") {
                overnight.isOpenAt(LocalDateTime.of(2026, 7, 7, 1, 0)) shouldBe true
            }

            test("다음 날 마감 시각 이후면 false") {
                overnight.isOpenAt(LocalDateTime.of(2026, 7, 7, 3, 0)) shouldBe false
            }

            test("개점일 개점 시각 전이면 false") {
                overnight.isOpenAt(LocalDateTime.of(2026, 7, 6, 21, 0)) shouldBe false
            }
        }

        context("한 요일에 슬롯이 여러 개인 경우 (브레이크 타임)") {
            val withBreak =
                BusinessHours(
                    listOf(
                        BusinessHourSlot(
                            days = listOf(DayOfWeek.MONDAY),
                            isOpen = true,
                            open = LocalTime.of(10, 0),
                            close = LocalTime.of(14, 0),
                        ),
                        BusinessHourSlot(
                            days = listOf(DayOfWeek.MONDAY),
                            isOpen = true,
                            open = LocalTime.of(17, 0),
                            close = LocalTime.of(21, 0),
                        ),
                    ),
                )

            test("브레이크 타임이면 false") {
                withBreak.isOpenAt(LocalDateTime.of(2026, 7, 6, 15, 0)) shouldBe false
            }

            test("두 번째 슬롯 안이면 true") {
                withBreak.isOpenAt(LocalDateTime.of(2026, 7, 6, 18, 0)) shouldBe true
            }
        }
    })
