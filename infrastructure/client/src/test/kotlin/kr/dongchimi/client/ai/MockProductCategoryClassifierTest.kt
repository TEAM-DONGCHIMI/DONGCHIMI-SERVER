package kr.dongchimi.client.ai

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kr.dongchimi.core.product.ProductCategory
import java.time.Duration

class MockProductCategoryClassifierTest :
    FunSpec({
        val classifier = MockProductCategoryClassifier(ImportMockProperties(latency = Duration.ZERO))

        test("사전에 있는 키워드가 포함된 상품명은 해당 카테고리로 분류한다") {
            classifier.classify("통통 아삭 콩나물 (300g)") shouldBe ProductCategory.VEGETABLE_FRUIT
        }

        test("다른 카테고리 키워드도 각각 분류한다") {
            classifier.classify("서울우유 1L") shouldBe ProductCategory.DAIRY
            classifier.classify("국내산 삼겹살 500g") shouldBe ProductCategory.MEAT_EGG
            classifier.classify("생수 2L") shouldBe ProductCategory.BEVERAGE_ALCOHOL
        }

        test("사전에 없는 상품명은 null을 반환한다 (매칭 실패)") {
            classifier.classify("정체불명의 신상품 XYZ-123").shouldBeNull()
        }

        test("같은 입력에 항상 같은 결과를 반환한다 (결정적)") {
            val name = "아삭한 오이 5입"

            val first = classifier.classify(name)
            val second = classifier.classify(name)

            first shouldBe second
            first shouldBe ProductCategory.VEGETABLE_FRUIT
        }
    })
