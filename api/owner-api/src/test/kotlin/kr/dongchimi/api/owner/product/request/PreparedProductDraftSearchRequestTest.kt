package kr.dongchimi.api.owner.product.request

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kr.dongchimi.core.common.exception.CommonErrorCode
import kr.dongchimi.core.common.exception.CoreException
import kr.dongchimi.core.product.ProductCategory

class PreparedProductDraftSearchRequestTest :
    FunSpec({
        test("toSearchCondition: 카테고리 문자열을 ProductCategory로 변환한다") {
            val request = PreparedProductDraftSearchRequest(categories = listOf("SEAFOOD", "MEAT_EGG"))

            val condition = request.toSearchCondition()

            condition.categories shouldBe listOf(ProductCategory.SEAFOOD, ProductCategory.MEAT_EGG)
        }

        test("toSearchCondition: 잘못된 카테고리면 INVALID_INPUT") {
            val request = PreparedProductDraftSearchRequest(categories = listOf("INVALID"))

            val exception = shouldThrow<CoreException> { request.toSearchCondition() }

            exception.errorCode shouldBe CommonErrorCode.INVALID_INPUT
        }

        test("toSearchCondition: 검색어는 trim되고 공백만 있으면 null이 된다") {
            val request = PreparedProductDraftSearchRequest(search = "  ")

            val condition = request.toSearchCondition()

            condition.search shouldBe null
        }

        test("toSearchCondition: 검색어 앞뒤 공백이 제거된다") {
            val request = PreparedProductDraftSearchRequest(search = "  삼겹살  ")

            val condition = request.toSearchCondition()

            condition.search shouldBe "삼겹살"
        }
    })
