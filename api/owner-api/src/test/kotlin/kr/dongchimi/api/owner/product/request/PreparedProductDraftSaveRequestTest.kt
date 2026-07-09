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

class PreparedProductDraftSaveRequestTest :
    FunSpec({
        test("임시저장할 상품이 없으면 INVALID_INPUT") {
            val request = PreparedProductDraftSaveRequest(emptyList())

            val exception = shouldThrow<CoreException> { request.toCommands() }

            exception.errorCode shouldBe CommonErrorCode.INVALID_INPUT
        }

        test("preparedProductId가 중복되면 INVALID_INPUT") {
            val request = PreparedProductDraftSaveRequest(listOf(draft(id = 1L), draft(id = 1L)))

            val exception = shouldThrow<CoreException> { request.toCommands() }

            exception.errorCode shouldBe CommonErrorCode.INVALID_INPUT
        }

        test("정가만 있고 판매 가격이 없으면 INVALID_INPUT") {
            val exception = shouldThrow<CoreException> { draft(discountedPrice = null).toCommand() }

            exception.errorCode shouldBe CommonErrorCode.INVALID_INPUT
        }

        test("판매 가격이 정가보다 크면 INVALID_INPUT") {
            val exception =
                shouldThrow<CoreException> {
                    draft(originalPrice = BigDecimal("4000"), discountedPrice = BigDecimal("5000")).toCommand()
                }

            exception.errorCode shouldBe CommonErrorCode.INVALID_INPUT
        }

        test("시작일만 있고 종료일이 없으면 INVALID_INPUT") {
            val exception = shouldThrow<CoreException> { draft(discountEndDate = null).toCommand() }

            exception.errorCode shouldBe CommonErrorCode.INVALID_INPUT
        }

        test("시작일이 종료일보다 나중이면 INVALID_INPUT") {
            val exception =
                shouldThrow<CoreException> {
                    draft(discountStartDate = LocalDate.of(2025, 8, 20), discountEndDate = LocalDate.of(2025, 8, 1)).toCommand()
                }

            exception.errorCode shouldBe CommonErrorCode.INVALID_INPUT
        }

        test("상품명이 공백이면 INVALID_INPUT") {
            val exception = shouldThrow<CoreException> { draft(name = " ").toCommand() }

            exception.errorCode shouldBe CommonErrorCode.INVALID_INPUT
        }

        test("preparedProductId 외 모든 필드가 비어도 통과한다") {
            val command =
                PreparedProductDraftSaveRequest.PreparedProductDraftRequest(preparedProductId = 1L).toCommand()

            command.id shouldBe 1L
            command.name shouldBe null
            command.price shouldBe null
            command.discountPeriod shouldBe null
        }

        test("dealType을 보내지 않으면 PERIODIC으로 채워진다") {
            draft(dealType = null).toCommand().dealType shouldBe DealType.PERIODIC
        }

        test("정상이면 Price·DiscountPeriod VO로 변환된다") {
            val command = draft().toCommand()

            command.price!!.originalPrice shouldBe BigDecimal("5000")
            command.price!!.discountedPrice shouldBe BigDecimal("4000")
            command.discountPeriod!!.discountStartDate shouldBe LocalDate.of(2025, 8, 1)
            command.discountPeriod!!.discountEndDate shouldBe LocalDate.of(2025, 8, 16)
        }
    })

private fun draft(
    id: Long = 1L,
    name: String? = "삼겹살 500g",
    originalPrice: BigDecimal? = BigDecimal("5000"),
    discountedPrice: BigDecimal? = BigDecimal("4000"),
    discountStartDate: LocalDate? = LocalDate.of(2025, 8, 1),
    discountEndDate: LocalDate? = LocalDate.of(2025, 8, 16),
    dealType: DealType? = null,
): PreparedProductDraftSaveRequest.PreparedProductDraftRequest =
    PreparedProductDraftSaveRequest.PreparedProductDraftRequest(
        preparedProductId = id,
        name = name,
        thumbnailUrl = "https://cdn.example.com/products/1.png",
        originalPrice = originalPrice,
        discountedPrice = discountedPrice,
        category = ProductCategory.MEAT_EGG,
        promotionalPhrase = null,
        discountStartDate = discountStartDate,
        discountEndDate = discountEndDate,
        dealType = dealType,
    )
