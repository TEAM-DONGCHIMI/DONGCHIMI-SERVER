package kr.dongchimi.api.owner.product.request

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kr.dongchimi.api.core.common.exception.InvalidInputException
import kr.dongchimi.core.product.DealType
import kr.dongchimi.core.product.ProductCategory
import kr.dongchimi.core.product.ProductSortType

class OwnerProductListRequestTest :
    FunSpec({
        test("type이 없으면 예외를 던진다") {
            shouldThrow<InvalidInputException> {
                OwnerProductListRequest().toSearchCondition()
            }.message shouldBe "판매 유형은 필수입니다."
        }

        test("type이 유효하지 않으면 예외를 던진다") {
            shouldThrow<InvalidInputException> {
                OwnerProductListRequest(type = "INVALID").toSearchCondition()
            }.message shouldBe "판매 유형이 올바르지 않습니다."
        }

        test("sort를 생략하면 CATEGORY가 기본값이다") {
            val condition = OwnerProductListRequest(type = "DAILY").toSearchCondition()

            condition.sort shouldBe ProductSortType.CATEGORY
        }

        test("sort가 유효하지 않으면 예외를 던진다") {
            shouldThrow<InvalidInputException> {
                OwnerProductListRequest(type = "DAILY", sort = "INVALID").toSearchCondition()
            }.message shouldBe "유효하지 않은 정렬 기준입니다."
        }

        test("category가 blank이면 전체 조회로 처리한다") {
            val condition = OwnerProductListRequest(type = "DAILY", category = "").toSearchCondition()

            condition.category shouldBe null
        }

        test("category가 유효한 값이면 해당 enum으로 변환한다") {
            val condition = OwnerProductListRequest(type = "DAILY", category = "SEAFOOD").toSearchCondition()

            condition.category shouldBe ProductCategory.SEAFOOD
        }

        test("category가 유효하지 않으면 예외를 던진다") {
            shouldThrow<InvalidInputException> {
                OwnerProductListRequest(type = "DAILY", category = "INVALID").toSearchCondition()
            }.message shouldBe "카테고리가 올바르지 않습니다."
        }

        test("cursor가 0 이하면 예외를 던진다") {
            shouldThrow<InvalidInputException> {
                OwnerProductListRequest(type = "DAILY", cursor = 0L).toSearchCondition()
            }.message shouldBe "cursor는 1 이상이어야 합니다."
        }

        test("size를 생략하면 기본값 12를 사용한다") {
            val condition = OwnerProductListRequest(type = "DAILY").toSearchCondition()

            condition.size shouldBe 12
        }

        test("size가 최대치를 초과하면 예외를 던진다") {
            shouldThrow<InvalidInputException> {
                OwnerProductListRequest(type = "DAILY", size = 61).toSearchCondition()
            }.message shouldBe "조회 개수는 1 이상 60개 이하여야 합니다."
        }

        test("정상 입력이면 조건으로 변환된다") {
            val condition =
                OwnerProductListRequest(
                    type = "PERIODIC",
                    sort = "VIEW_COUNT",
                    category = "MEAT_EGG",
                    cursor = 10L,
                    size = 20,
                ).toSearchCondition()

            condition.dealType shouldBe DealType.PERIODIC
            condition.sort shouldBe ProductSortType.VIEW_COUNT
            condition.category shouldBe ProductCategory.MEAT_EGG
            condition.cursor shouldBe 10L
            condition.size shouldBe 20
        }
    })
