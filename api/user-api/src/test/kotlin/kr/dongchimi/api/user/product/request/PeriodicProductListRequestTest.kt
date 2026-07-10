package kr.dongchimi.api.user.product.request

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kr.dongchimi.api.core.common.exception.InvalidInputException
import kr.dongchimi.core.product.ProductCategory

class PeriodicProductListRequestTest :
    FunSpec({
        test("category와 cursor, size를 생략하면 전체 카테고리·첫 페이지·기본 12개를 사용한다") {
            val condition = PeriodicProductListRequest().toSearchCondition()

            condition.category shouldBe null
            condition.cursor shouldBe null
            condition.size shouldBe 12
        }

        test("category가 유효한 값이면 해당 enum으로 변환한다") {
            val condition = PeriodicProductListRequest(category = "SEAFOOD").toSearchCondition()

            condition.category shouldBe ProductCategory.SEAFOOD
        }

        test("category가 유효하지 않으면 예외를 던진다") {
            shouldThrow<InvalidInputException> {
                PeriodicProductListRequest(category = "INVALID").toSearchCondition()
            }.message shouldBe "카테고리가 올바르지 않습니다."
        }

        test("cursor가 0 이하면 예외를 던진다") {
            shouldThrow<InvalidInputException> {
                PeriodicProductListRequest(cursor = 0L).toSearchCondition()
            }.message shouldBe "cursor는 1 이상이어야 합니다."
        }

        test("size가 0 이하면 예외를 던진다") {
            shouldThrow<InvalidInputException> {
                PeriodicProductListRequest(size = 0).toSearchCondition()
            }.message shouldBe "조회 개수는 1 이상 60개 이하여야 합니다."
        }

        test("size가 최대치를 초과하면 예외를 던진다") {
            shouldThrow<InvalidInputException> {
                PeriodicProductListRequest(size = 61).toSearchCondition()
            }.message shouldBe "조회 개수는 1 이상 60개 이하여야 합니다."
        }
    })
