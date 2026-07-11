package kr.dongchimi.client.ai

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/** Gemini `generateContent` 응답 — 실제 텍스트는 candidates[0].content.parts[0].text에 있다. */
@JsonIgnoreProperties(ignoreUnknown = true)
data class GeminiGenerateContentResponse(
    val candidates: List<GeminiCandidate> = emptyList(),
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class GeminiCandidate(
    val content: GeminiContent? = null,
)
