package kr.dongchimi.client.ai

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kr.dongchimi.core.product.ProductCategory
import java.time.Duration

class MockProductImageMatcherTest :
    FunSpec({
        val matcher = MockProductImageMatcher(ImportMockProperties(latency = Duration.ZERO))

        test("category가 null이면 매칭도 null이다 (카테고리 분류부터 실패한 상품)") {
            matcher.match("정체불명의 신상품", category = null).shouldBeNull()
        }

        test("category가 있으면 그 카테고리의 기본 이미지 URL을 반환한다") {
            val url = matcher.match("콩나물", category = ProductCategory.VEGETABLE_FRUIT)

            url.shouldNotBeNull()
        }

        test("같은 카테고리는 항상 같은 URL을 반환한다 (결정적)") {
            val first = matcher.match("콩나물", ProductCategory.VEGETABLE_FRUIT)
            val second = matcher.match("오이", ProductCategory.VEGETABLE_FRUIT)

            first shouldBe second
        }

        test("카테고리가 다르면 다른 URL을 반환한다") {
            val vegetableUrl = matcher.match("콩나물", ProductCategory.VEGETABLE_FRUIT)
            val dairyUrl = matcher.match("우유", ProductCategory.DAIRY)

            vegetableUrl shouldNotBe dairyUrl
        }
    })
