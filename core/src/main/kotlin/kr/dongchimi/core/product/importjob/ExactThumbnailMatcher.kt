package kr.dongchimi.core.product.importjob

import kr.dongchimi.core.admin.DefaultProductThumbnail
import kr.dongchimi.core.product.ProductCategory
import org.springframework.stereotype.Component

/**
 * AI 호출 전에 상품명이 후보 썸네일 이름과 정규화 후 정확히 일치하는 행을 먼저 확정한다.
 * 같은 category 안에서 정규화된 이름이 유일한 후보에만 대응할 때만 확정하고,
 * 동일 이름 후보가 여럿이면 모호한 것으로 보아 AI 매칭으로 위임한다.
 */
@Component
class ExactThumbnailMatcher {
    fun match(
        items: List<ProductImageMatchItem>,
        candidates: List<DefaultProductThumbnail>,
    ): Map<Int, String> {
        val urlByCategoryAndName = buildIndex(candidates)

        return items
            .mapNotNull { item ->
                val url = urlByCategoryAndName[item.category to normalize(item.productName)]
                url?.let { item.id to it }
            }.toMap()
    }

    private fun buildIndex(candidates: List<DefaultProductThumbnail>): Map<Pair<ProductCategory, String>, String> =
        candidates
            .groupBy { it.category to normalize(it.name) }
            .filterValues { it.size == 1 } // 동일 정규화 이름 후보가 2건 이상이면 모호 → 인덱스에서 제외
            .mapValues { (_, group) -> group.single().thumbnailUrl }

    private fun normalize(raw: String): String = raw.trim().replace(Regex("\\s+"), " ").lowercase()
}
