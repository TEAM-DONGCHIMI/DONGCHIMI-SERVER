package kr.dongchimi.client.ai

import com.google.genai.Client
import com.google.genai.errors.ApiException
import com.google.genai.errors.GenAiIOException
import com.google.genai.types.Content
import com.google.genai.types.GenerateContentConfig
import com.google.genai.types.Part
import com.google.genai.types.Schema
import com.google.genai.types.ThinkingConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import tools.jackson.core.JacksonException
import tools.jackson.core.type.TypeReference
import tools.jackson.databind.ObjectMapper

/** 일시적 HTTP 실패(타임아웃·5xx·429) — 호출부가 작업 전체를 실패시키고 큐 재시도에 맡긴다. */
class GeminiRequestException(
    cause: Throwable,
) : RuntimeException(cause)

/** 200이지만 응답을 목표 타입으로 파싱할 수 없음 — 재시도하지 않고 호출부가 해당 청크만 null로 강등한다. */
class GeminiResponseFormatException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)

@Component
@ConditionalOnProperty(name = ["import.ai.provider"], havingValue = "gemini")
class GeminiClient(
    private val genAiClient: Client,
    private val geminiProperties: GeminiProperties,
    private val objectMapper: ObjectMapper,
) {
    /**
     * systemInstruction + userContent로 generateContent를 호출하고, [responseSchema]를 만족하는 JSON 응답을 [type]으로 파싱해 돌려준다.
     * HTTP 실패는 [GeminiRequestException], 200이지만 파싱 불가면 [GeminiResponseFormatException]을 던진다.
     */
    suspend fun <T> generate(
        systemInstruction: String,
        userContent: String,
        responseSchema: Schema,
        type: TypeReference<T>,
    ): T {
        val text = requestText(systemInstruction, userContent, responseSchema)

        return try {
            objectMapper.readValue(text, type)
        } catch (e: JacksonException) {
            throw GeminiResponseFormatException("Gemini 응답 파싱 실패", e)
        }
    }

    private suspend fun requestText(
        systemInstruction: String,
        userContent: String,
        responseSchema: Schema,
    ): String =
        withContext(Dispatchers.IO) {
            val config =
                GenerateContentConfig
                    .builder()
                    .systemInstruction(Content.fromParts(Part.fromText(systemInstruction)))
                    .responseMimeType("application/json")
                    .responseSchema(responseSchema)
                    .thinkingConfig(ThinkingConfig.builder().thinkingLevel(geminiProperties.thinkingLevel).build())
                    .build()

            try {
                genAiClient.models
                    .generateContent(geminiProperties.model, userContent, config)
                    .text()
                    ?: throw GeminiResponseFormatException("Gemini 응답에 text가 없음")
            } catch (e: ApiException) {
                throw GeminiRequestException(e)
            } catch (e: GenAiIOException) {
                throw GeminiRequestException(e)
            }
        }
}
