package kr.dongchimi.client.ai

/**
 * Gemini `responseSchema`가 쓰는 OpenAPI 서브셋 스키마. `type`은 "OBJECT"/"ARRAY"/"STRING"/"INTEGER" 등 대문자 값을 그대로 넣는다.
 */
data class GeminiSchema(
    val type: String,
    val properties: Map<String, GeminiSchema>? = null,
    val items: GeminiSchema? = null,
    val enum: List<String>? = null,
    val required: List<String>? = null,
)
