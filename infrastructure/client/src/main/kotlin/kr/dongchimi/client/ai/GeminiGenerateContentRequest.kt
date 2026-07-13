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

/** 2.5 계열의 사고 깊이 설정. `generationConfig.thinkingConfig.thinkingBudget`로 중첩돼야 한다 — 최상위 필드가 아니다. */
data class GeminiThinkingConfig(
    /** 사고 토큰 예산. 0=비활성화, -1=모델 자동(dynamic). */
    val thinkingBudget: Int,
)
