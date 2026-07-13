package kr.dongchimi.client.ai

import com.google.genai.types.Schema
import com.google.genai.types.Type
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kr.dongchimi.core.admin.DefaultProductThumbnail
import kr.dongchimi.core.admin.DefaultProductThumbnailRepository
import kr.dongchimi.core.product.ProductCategory
import kr.dongchimi.core.product.importjob.ProductImageMatchItem
import kr.dongchimi.core.product.importjob.ProductImageMatcher
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import tools.jackson.core.type.TypeReference
import tools.jackson.databind.ObjectMapper

private val logger = KotlinLogging.logger {}

/**
 * 한 청크에 여러 category가 섞일 수 있어(청크는 category 무관으로 나뉜다), 청크에 실제로 등장한
 * category들의 후보만 한 번의 쿼리로 조회해 프롬프트에 담는다. 각 항목은 자기 category 후보 그룹에서만 매칭된다.
 */
@Component
@ConditionalOnProperty(name = ["import.ai.provider"], havingValue = "gemini")
class GeminiProductImageMatcher(
    private val geminiClient: GeminiClient,
    private val defaultProductThumbnailRepository: DefaultProductThumbnailRepository,
    private val objectMapper: ObjectMapper,
) : ProductImageMatcher {
    override suspend fun match(items: List<ProductImageMatchItem>): Map<Int, String?> {
        val presentCategories = items.map { it.category }.toSet()
        val candidatesByCategory =
            withContext(Dispatchers.IO) { defaultProductThumbnailRepository.findAllByCategoryIn(presentCategories) }
                .groupBy { it.category }
        if (candidatesByCategory.isEmpty()) return emptyMap() // 후보가 아예 없으면 호출 자체를 생략 → 전부 null

        val userContent =
            objectMapper.writeValueAsString(
                items.map { mapOf("id" to it.id, "name" to it.productName, "category" to it.category.name) },
            )

        val parsed: List<MatchResultItem> =
            try {
                geminiClient.generate(
                    systemInstruction(candidatesByCategory),
                    userContent,
                    RESPONSE_SCHEMA,
                    object : TypeReference<List<MatchResultItem>>() {},
                )
            } catch (e: GeminiResponseFormatException) {
                logger.warn(e) { "이미지 매칭 응답 파싱 실패 — 이 청크(${items.size}건)를 매칭 실패로 강등한다" }
                return emptyMap()
            }

        val allCandidates = candidatesByCategory.values.flatten()
        val urlById = allCandidates.associate { it.id to it.thumbnailUrl }
        val candidateCategoryById = allCandidates.associate { it.id to it.category }
        val itemCategoryById = items.associate { it.id to it.category }

        return parsed.associate { r ->
            val candidateId = r.candidateId.toLong()
            // 후보 목록 밖 id(-1 포함, 환각)이거나 항목의 category와 다른 category의 후보를 골랐으면 null
            val matched = urlById[candidateId]?.takeIf { candidateCategoryById[candidateId] == itemCategoryById[r.id] }
            r.id to matched
        }
    }

    private fun systemInstruction(candidatesByCategory: Map<ProductCategory, List<DefaultProductThumbnail>>) =
        """
        당신은 마트 상품명에 가장 잘 어울리는 대표 이미지를 후보 목록에서 골라주는 도우미입니다.
        각 상품에는 category가 붙어 있습니다. 반드시 그 상품의 category에 해당하는 후보 목록 안에서만 고르세요.

        카테고리별 이미지 후보 목록:
        ${candidatesByCategory.entries.joinToString("\n") { (category, candidates) ->
            "[${category.name}] " + candidates.joinToString(", ") { "id ${it.id}=${it.name}" }
        }}

        규칙:
        1. 각 상품(id, name, category)에 대해, 그 category 후보 중 상품명과 의미적으로 가장 가까운 후보의 id를 candidateId로 반환하세요.
        2. 상품의 category 후보 중 적절한 것이 없으면 candidateId를 -1로 반환하세요(스키마에 null이 없어 -1을 "매칭 없음"으로 씁니다).
        3. candidateId는 반드시 그 상품 category의 후보 목록에 실제로 있는 id이거나 -1이어야 합니다. 다른 category 후보나 목록에 없는 id를 쓰지 마세요.
        4. 입력받은 모든 상품 id에 대해 정확히 하나의 결과를 반환하세요.
        5. 지정된 JSON 스키마만 출력하고, 설명이나 다른 텍스트를 추가하지 마세요.
        """.trimIndent()

    companion object {
        private val RESPONSE_SCHEMA: Schema =
            Schema
                .builder()
                .type(Type.Known.ARRAY)
                .items(
                    Schema
                        .builder()
                        .type(Type.Known.OBJECT)
                        .properties(
                            mapOf(
                                "id" to Schema.builder().type(Type.Known.INTEGER).build(),
                                "candidateId" to Schema.builder().type(Type.Known.INTEGER).build(),
                            ),
                        ).required("id", "candidateId")
                        .build(),
                ).build()
    }
}

private data class MatchResultItem(
    val id: Int,
    val candidateId: Int,
)
