package kr.dongchimi.api.owner.product.request

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kr.dongchimi.core.common.exception.CommonErrorCode
import kr.dongchimi.core.common.exception.CoreException
import kr.dongchimi.core.product.DealType
import kr.dongchimi.core.product.ProductCategory
import java.math.BigDecimal
import java.time.LocalDate

class DailyProductRegisterRequestTest :
    FunSpec({
        fun request(
            thumbnailUrl: String? = "https://static.dongchimi.kr/products/test.png",
            name: String = "토마토",
            originalPrice: BigDecimal = BigDecimal("5000"),
            discountedPrice: BigDecimal = BigDecimal("4500"),
            discountStartDate: LocalDate = LocalDate.of(2026, 6, 30),
            discountEndDate: LocalDate = LocalDate.of(2026, 7, 10),
        ) = DailyProductRegisterRequest(
            thumbnailUrl = thumbnailUrl,
            name = name,
            category = ProductCategory.VEGETABLE_FRUIT,
            promotionalPhrase = "멋쟁이 토마토",
            originalPrice = originalPrice,
            discountedPrice = discountedPrice,
            discountStartDate = discountStartDate,
            discountEndDate = discountEndDate,
        )

        test("상품명이 공백이면 INVALID_INPUT") {
            val exception = shouldThrow<CoreException> { request(name = " ").toCommand() }

            exception.errorCode shouldBe CommonErrorCode.INVALID_INPUT
        }

        test("판매가가 정가보다 크면 INVALID_INPUT") {
            val exception =
                shouldThrow<CoreException> {
                    request(originalPrice = BigDecimal("4000"), discountedPrice = BigDecimal("5000")).toCommand()
                }

            exception.errorCode shouldBe CommonErrorCode.INVALID_INPUT
        }

        test("가격이 음수면 INVALID_INPUT") {
            val exception = shouldThrow<CoreException> { request(discountedPrice = BigDecimal("-1")).toCommand() }

            exception.errorCode shouldBe CommonErrorCode.INVALID_INPUT
        }

        test("시작일이 종료일보다 나중이면 INVALID_INPUT") {
            val exception =
                shouldThrow<CoreException> {
                    request(
                        discountStartDate = LocalDate.of(2026, 7, 10),
                        discountEndDate = LocalDate.of(2026, 6, 30),
                    ).toCommand()
                }

            exception.errorCode shouldBe CommonErrorCode.INVALID_INPUT
        }

        test("정상이면 DAILY 커맨드로 변환된다") {
            val command = request().toCommand()

            command.toProduct(marketId = 10L).dealType shouldBe DealType.DAILY
            command.thumbnailUrl shouldBe "https://static.dongchimi.kr/products/test.png"
            command.price.originalPrice shouldBe BigDecimal("5000")
        }

        test("썸네일이 공백이면 null로 변환된다") {
            request(thumbnailUrl = " ").toCommand().thumbnailUrl shouldBe null
        }
    })
