package kr.dongchimi.client.ai

import com.google.genai.types.Schema
import com.google.genai.types.Type
import io.github.oshai.kotlinlogging.KotlinLogging
import kr.dongchimi.core.product.ProductCategory
import kr.dongchimi.core.product.importjob.ProductCategoryClassifier
import kr.dongchimi.core.product.importjob.ProductCategoryClassifyItem
import kr.dongchimi.core.product.toProductCategoryOrNull
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import tools.jackson.core.type.TypeReference
import tools.jackson.databind.ObjectMapper

private val logger = KotlinLogging.logger {}

@Component
@ConditionalOnProperty(name = ["import.ai.provider"], havingValue = "gemini")
class GeminiProductCategoryClassifier(
    private val geminiClient: GeminiClient,
    private val objectMapper: ObjectMapper,
) : ProductCategoryClassifier {
    override suspend fun classify(items: List<ProductCategoryClassifyItem>): Map<Int, ProductCategory?> {
        // 상품명은 점주 업로드 엑셀에서 온 임의 문자열 → 문자열 보간 금지, 반드시 직렬화로 이스케이프한다.
        val userContent = objectMapper.writeValueAsString(items.map { mapOf("id" to it.id, "name" to it.productName) })

        val parsed: List<ClassifyResultItem> =
            try {
                geminiClient.generate(
                    SYSTEM_INSTRUCTION,
                    userContent,
                    RESPONSE_SCHEMA,
                    object : TypeReference<List<ClassifyResultItem>>() {},
                )
            } catch (e: GeminiResponseFormatException) {
                logger.warn(e) { "카테고리 분류 응답 파싱 실패 — 이 청크(${items.size}건)를 분류 실패로 강등한다" }
                return emptyMap()
            }

        // "UNKNOWN"이나 스키마 밖 문자열은 toProductCategoryOrNull()이 null로 처리 → 분류 실패
        return parsed.associate { it.id to it.category.toProductCategoryOrNull() }
    }

    companion object {
        private val SYSTEM_INSTRUCTION =
            """
            당신은 마트 전단 상품명을 보고 카테고리를 분류하는 도우미입니다.
            아래 카테고리 중 정확히 하나로 각 상품을 분류하세요.

            ${ProductCategory.entries.joinToString("\n") { "- ${it.name}: ${it.displayName}" }}
            - UNKNOWN: 상품명만으로는 카테고리를 판단할 수 없는 경우

            규칙:
            1. 입력으로 받은 모든 id에 대해 정확히 하나의 결과를 반환하세요. id를 누락하거나 새로 만들지 마세요.
            2. 상품명이 모호하거나 마트 판매 상품처럼 보이지 않으면 ETC가 아니라 UNKNOWN을 선택하세요.
               ETC는 "명백히 마트 상품이지만 위 카테고리 중 어디에도 맞지 않는 경우"에만 씁니다.
            3. 브랜드명·용량·단위 표기는 무시하고 상품의 본질로 판단하세요. 예: "서울우유 1L" → DAIRY.
            4. 지정된 JSON 스키마만 출력하고, 설명이나 다른 텍스트를 추가하지 마세요.
            """.trimIndent()

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
                                "category" to
                                    Schema
                                        .builder()
                                        .type(Type.Known.STRING)
                                        .enum_(ProductCategory.entries.map { it.name } + "UNKNOWN")
                                        .build(),
                            ),
                        ).required("id", "category")
                        .build(),
                ).build()
    }
}

private data class ClassifyResultItem(
    val id: Int,
    val category: String,
)
