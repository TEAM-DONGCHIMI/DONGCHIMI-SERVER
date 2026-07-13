package kr.dongchimi.client.ai

data class GeminiGenerateContentRequest(
    val systemInstruction: GeminiContent,
    val contents: List<GeminiContent>,
    val generationConfig: GeminiGenerationConfig,
)

data class GeminiContent(
    val parts: List<GeminiPart>,
)

data class GeminiPart(
    val text: String,
)

data class GeminiGenerationConfig(
    val responseMimeType: String,
    val responseSchema: GeminiSchema,
    /** 3.x 모델의 thinking_level enum. null이면 모델 기본값(medium). */
    val thinkingLevel: String? = null,
)
