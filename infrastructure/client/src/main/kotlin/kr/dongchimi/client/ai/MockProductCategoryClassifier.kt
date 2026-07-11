package kr.dongchimi.client.ai

import kotlinx.coroutines.delay
import kr.dongchimi.core.product.ProductCategory
import kr.dongchimi.core.product.importjob.ProductCategoryClassifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

/**
 * 실제 AI 카테고리 분류 호출이 아직 없어(범위 밖) 키워드 사전으로 대체한다.
 * 랜덤이면 테스트가 흔들리고, 항상 성공하면 CATEGORY_MISSING 경로가 한 번도 실행되지 않아
 * 사전에 없는 상품명은 의도적으로 null을 반환한다. ETC는 "그 무엇에도 안 걸림"의 의미라
 * 사전에 넣지 않는다 — 넣으면 미스가 없어져 null 분기가 죽는다.
 */
@Component
@ConditionalOnProperty(name = ["import.ai.provider"], havingValue = "mock", matchIfMissing = true)
class MockProductCategoryClassifier(
    private val properties: ImportMockProperties,
) : ProductCategoryClassifier {
    override suspend fun classify(productName: String): ProductCategory? {
        delay(properties.latency.toMillis())

        return CATEGORY_KEYWORDS.entries
            .firstOrNull { (_, keywords) -> keywords.any { productName.contains(it) } }
            ?.key
    }

    companion object {
        private val CATEGORY_KEYWORDS: Map<ProductCategory, List<String>> =
            mapOf(
                ProductCategory.VEGETABLE_FRUIT to
                    listOf("사과", "배", "바나나", "포도", "딸기", "토마토", "콩나물", "상추", "양파", "감자", "고구마", "당근", "오이", "배추"),
                ProductCategory.MEAT_EGG to listOf("삼겹살", "목살", "닭고기", "달걀", "계란", "소고기", "돼지고기", "안심", "등심"),
                ProductCategory.SEAFOOD to listOf("고등어", "갈치", "오징어", "새우", "굴", "조기", "멸치", "김"),
                ProductCategory.DAIRY to listOf("우유", "치즈", "요거트", "요구르트", "버터", "생크림"),
                ProductCategory.CONVENIENCE_FOOD to listOf("도시락", "삼각김밥", "김밥", "샌드위치", "냉동밥"),
                ProductCategory.PROCESSED_FOOD to listOf("라면", "만두", "햄", "소시지", "통조림", "김치"),
                ProductCategory.BEVERAGE_ALCOHOL to listOf("맥주", "소주", "와인", "콜라", "사이다", "주스", "생수"),
                ProductCategory.HOUSEHOLD_GOODS to listOf("휴지", "세제", "수세미", "칫솔", "비누"),
            )
    }
}
