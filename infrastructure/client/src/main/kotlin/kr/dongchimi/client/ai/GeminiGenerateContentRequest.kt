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
    val thinkingConfig: GeminiThinkingConfig? = null,
)

/** 3.x 모델의 사고 깊이 설정. `generationConfig.thinkingConfig.thinkingLevel`로 중첩돼야 한다 — 최상위 필드가 아니다. */
data class GeminiThinkingConfig(
    /** minimal/low/medium(모델 기본)/high. */
    val thinkingLevel: String,
)
