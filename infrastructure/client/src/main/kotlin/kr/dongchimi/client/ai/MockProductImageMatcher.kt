package kr.dongchimi.client.ai

import kotlinx.coroutines.delay
import kr.dongchimi.core.product.ProductCategory
import kr.dongchimi.core.product.import.ProductImageMatcher
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

/**
 * 실제 AI 이미지 매칭 호출이 아직 없어(범위 밖) 카테고리별 기본 이미지로 대체한다.
 * category가 null이면(카테고리 분류부터 실패한 상품) 매칭도 실패로 처리한다.
 */
@Component
@ConditionalOnProperty(name = ["import.ai.provider"], havingValue = "mock", matchIfMissing = true)
class MockProductImageMatcher(
    private val properties: ImportMockProperties,
) : ProductImageMatcher {
    override suspend fun match(
        productName: String,
        category: ProductCategory?,
    ): String? {
        delay(properties.latency.toMillis())

        return category?.let { DEFAULT_THUMBNAILS_BY_CATEGORY.getValue(it) }
    }

    companion object {
        private val DEFAULT_THUMBNAILS_BY_CATEGORY: Map<ProductCategory, String> =
            ProductCategory.entries.associateWith { "https://cdn.dongchimi.kr/default-thumbnails/${it.name.lowercase()}.jpg" }
    }
}
