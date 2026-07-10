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

class ProductUpdateRequestTest :
    FunSpec({
        fun request(
            type: DealType = DealType.DAILY,
            name: String = "삼겹살 500g",
            originalPrice: BigDecimal? = BigDecimal("22000"),
            discountedPrice: BigDecimal = BigDecimal("19500"),
            discountStartDate: LocalDate = LocalDate.of(2026, 6, 30),
            discountEndDate: LocalDate = LocalDate.of(2026, 7, 10),
        ) = ProductUpdateRequest(
            type = type,
            thumbnailUrl = "https://cdn.dongchimi.kr/products/201.png",
            name = name,
            category = ProductCategory.MEAT_EGG,
            promotionalPhrase = null,
            originalPrice = originalPrice,
            discountedPrice = discountedPrice,
            discountStartDate = discountStartDate,
            discountEndDate = discountEndDate,
        )

        test("상품명이 공백이면 INVALID_INPUT") {
            val exception = shouldThrow<CoreException> { request(name = " ").toCommand() }

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

        test("DAILY인데 originalPrice가 없으면 INVALID_INPUT") {
            val exception = shouldThrow<CoreException> { request(type = DealType.DAILY, originalPrice = null).toCommand() }

            exception.errorCode shouldBe CommonErrorCode.INVALID_INPUT
        }

        test("DAILY인데 할인가가 원가보다 크면 INVALID_INPUT") {
            val exception =
                shouldThrow<CoreException> {
                    request(originalPrice = BigDecimal("10000"), discountedPrice = BigDecimal("12000")).toCommand()
                }

            exception.errorCode shouldBe CommonErrorCode.INVALID_INPUT
        }

        test("DAILY 정상이면 정가·할인가가 반영된다") {
            val command = request(originalPrice = BigDecimal("22000"), discountedPrice = BigDecimal("19500")).toCommand()

            command.dealType shouldBe DealType.DAILY
            command.price.originalPrice shouldBe BigDecimal("22000")
            command.price.discountedPrice shouldBe BigDecimal("19500")
        }

        test("PERIODIC은 originalPrice 없이도 정상 변환되고 원가=판매가로 저장된다") {
            val command =
                request(type = DealType.PERIODIC, originalPrice = null, discountedPrice = BigDecimal("6900")).toCommand()

            command.dealType shouldBe DealType.PERIODIC
            command.price.originalPrice shouldBe BigDecimal("6900")
            command.price.discountedPrice shouldBe BigDecimal("6900")
            command.price.discountRate() shouldBe 0
        }
    })
