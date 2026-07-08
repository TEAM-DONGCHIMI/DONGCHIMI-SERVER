package kr.dongchimi.api.owner.product.request

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kr.dongchimi.core.common.exception.CommonErrorCode
import kr.dongchimi.core.common.exception.CoreException
import java.time.LocalDate

class ProductDiscountPeriodUpdateRequestTest :
    FunSpec({
        test("시작일이 종료일보다 나중이면 INVALID_INPUT") {
            val request =
                ProductDiscountPeriodUpdateRequest(
                    discountStartDate = LocalDate.of(2025, 8, 20),
                    discountEndDate = LocalDate.of(2025, 8, 1),
                    productIds = listOf(1L),
                )

            val exception = shouldThrow<CoreException> { request.toDiscountPeriod() }

            exception.errorCode shouldBe CommonErrorCode.INVALID_INPUT
        }

        test("정상이면 DiscountPeriod로 변환된다") {
            val request =
                ProductDiscountPeriodUpdateRequest(
                    discountStartDate = LocalDate.of(2025, 8, 1),
                    discountEndDate = LocalDate.of(2025, 8, 16),
                    productIds = listOf(1L, 2L),
                )

            val discountPeriod = request.toDiscountPeriod()

            discountPeriod.discountStartDate shouldBe LocalDate.of(2025, 8, 1)
            discountPeriod.discountEndDate shouldBe LocalDate.of(2025, 8, 16)
        }
    })
