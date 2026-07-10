package kr.dongchimi.core.product

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDate

class DiscountPeriodTest :
    FunSpec({
        val period = DiscountPeriod(LocalDate.of(2026, 7, 10), LocalDate.of(2026, 7, 20))

        test("includes: 시작일이면 true") {
            period.includes(LocalDate.of(2026, 7, 10)) shouldBe true
        }

        test("includes: 종료일이면 true") {
            period.includes(LocalDate.of(2026, 7, 20)) shouldBe true
        }

        test("includes: 기간 안이면 true") {
            period.includes(LocalDate.of(2026, 7, 15)) shouldBe true
        }

        test("includes: 시작일 이전이면 false") {
            period.includes(LocalDate.of(2026, 7, 9)) shouldBe false
        }

        test("includes: 종료일 이후면 false") {
            period.includes(LocalDate.of(2026, 7, 21)) shouldBe false
        }
    })
