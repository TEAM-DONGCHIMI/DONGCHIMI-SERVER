package kr.dongchimi.core.product

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.math.BigDecimal
import java.time.LocalDate

class DraftFailReasonResolverTest :
    FunSpec({
        val resolver = DraftFailReasonResolver()

        fun resolveOne(command: PreparedProductDraftSaveCommand): DraftFailReason? = resolver.resolve(listOf(command))[command.id]

        test("필수값이 모두 채워지면 실패 사유가 없다") {
            resolveOne(complete()) shouldBe null
        }

        test("홍보 문구는 선택이라 없어도 실패 사유가 없다") {
            resolveOne(complete().copy(promotionalPhrase = null)) shouldBe null
        }

        test("이미지가 없으면 THUMBNAIL_MISSING") {
            resolveOne(complete().copy(thumbnailUrl = null)) shouldBe DraftFailReason.THUMBNAIL_MISSING
        }

        test("카테고리가 없으면 CATEGORY_MISSING") {
            resolveOne(complete().copy(category = null)) shouldBe DraftFailReason.CATEGORY_MISSING
        }

        test("상품명이 없으면 NAME_MISSING") {
            resolveOne(complete().copy(name = null)) shouldBe DraftFailReason.NAME_MISSING
        }

        test("판매 가격이 없으면 PRICE_MISSING") {
            resolveOne(complete().copy(price = null)) shouldBe DraftFailReason.PRICE_MISSING
        }

        test("할인 기간이 없으면 DISCOUNT_PERIOD_MISSING") {
            resolveOne(complete().copy(discountPeriod = null)) shouldBe DraftFailReason.DISCOUNT_PERIOD_MISSING
        }

        test("여러 항목이 누락되면 우선순위가 가장 높은 사유 하나만 반환한다") {
            resolveOne(complete().copy(thumbnailUrl = null, name = null)) shouldBe DraftFailReason.THUMBNAIL_MISSING
        }

        test("모든 항목이 누락되면 THUMBNAIL_MISSING") {
            val empty =
                complete().copy(
                    name = null,
                    thumbnailUrl = null,
                    price = null,
                    category = null,
                    discountPeriod = null,
                )

            resolveOne(empty) shouldBe DraftFailReason.THUMBNAIL_MISSING
        }

        test("여러 건을 한 번에 받으면 id별로 사유를 매핑한다") {
            val commands =
                listOf(
                    complete(id = 1L),
                    complete(id = 2L).copy(thumbnailUrl = null),
                    complete(id = 3L).copy(price = null),
                )

            resolver.resolve(commands) shouldBe
                mapOf(
                    1L to null,
                    2L to DraftFailReason.THUMBNAIL_MISSING,
                    3L to DraftFailReason.PRICE_MISSING,
                )
        }

        test("빈 목록을 받으면 빈 결과를 반환한다") {
            resolver.resolve(emptyList()) shouldBe emptyMap()
        }
    })

private fun complete(id: Long = 1L): PreparedProductDraftSaveCommand =
    PreparedProductDraftSaveCommand(
        id = id,
        name = "삼겹살 500g",
        thumbnailUrl = "https://static.dongchimi.kr/test.png",
        price = Price(BigDecimal("5000"), BigDecimal("4000")),
        category = ProductCategory.MEAT_EGG,
        promotionalPhrase = "맛이 미쳤어요",
        discountPeriod = DiscountPeriod(LocalDate.of(2025, 8, 1), LocalDate.of(2025, 8, 16)),
        dealType = DealType.PERIODIC,
    )
