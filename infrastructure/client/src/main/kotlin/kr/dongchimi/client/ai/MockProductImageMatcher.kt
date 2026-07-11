package kr.dongchimi.client.ai

import kotlinx.coroutines.delay
import kr.dongchimi.core.product.ProductCategory
import kr.dongchimi.core.product.importjob.ProductImageMatchItem
import kr.dongchimi.core.product.importjob.ProductImageMatcher
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

/**
 * 실제 AI 이미지 매칭 호출이 아직 없어(범위 밖) 카테고리별 기본 이미지로 대체한다.
 * category가 null인(분류부터 실패한) 행은 호출 전에 걸러지므로 여기 오는 항목은 모두 유효한 category를 갖는다.
 */
@Component
@ConditionalOnProperty(name = ["import.ai.provider"], havingValue = "mock", matchIfMissing = true)
class MockProductImageMatcher(
    private val properties: ImportMockProperties,
) : ProductImageMatcher {
    override suspend fun match(items: List<ProductImageMatchItem>): Map<Int, String?> {
        delay(properties.latency.toMillis())

        return items.associate { it.id to DEFAULT_THUMBNAILS_BY_CATEGORY.getValue(it.category) }
    }

    companion object {
        private val DEFAULT_THUMBNAILS_BY_CATEGORY: Map<ProductCategory, String> =
            ProductCategory.entries.associateWith { "https://cdn.dongchimi.kr/default-thumbnails/${it.name.lowercase()}.jpg" }
    }
}
