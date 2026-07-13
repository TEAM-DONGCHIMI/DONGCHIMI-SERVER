package kr.dongchimi.client.ai

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException
import tools.jackson.core.JacksonException
import tools.jackson.core.type.TypeReference
import tools.jackson.databind.ObjectMapper

private val logger = KotlinLogging.logger {}

/** 일시적 HTTP 실패(타임아웃·5xx·429) — 재시도 소진 후에도 실패하면 작업 전체를 실패시키고 큐 재시도에 맡긴다. */
class GeminiRequestException(
    cause: Throwable,
) : RuntimeException(cause)

/** 200이지만 응답을 목표 타입으로 파싱할 수 없음 — 재시도하지 않고 호출부가 해당 청크만 null로 강등한다. */
class GeminiResponseFormatException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)

@Component
class GeminiClient(
    private val geminiProperties: GeminiProperties,
    @Qualifier("geminiRestClient") private val restClient: RestClient,
    private val objectMapper: ObjectMapper,
) {
    /**
     * systemInstruction + userContent로 generateContent를 호출하고, 스키마를 만족하는 JSON 응답을 [type]으로 파싱해 돌려준다.
     * HTTP 실패는 [GeminiRequestException], 200이지만 파싱 불가면 [GeminiResponseFormatException]을 던진다.
     */
    suspend fun <T> generate(
        systemInstruction: String,
        userContent: String,
        responseSchema: GeminiSchema,
        type: TypeReference<T>,
    ): T {
        val text = retryOnTransientFailure { requestText(systemInstruction, userContent, responseSchema) }

        return try {
            objectMapper.readValue(text, type)
        } catch (e: JacksonException) {
            throw GeminiResponseFormatException("Gemini 응답 파싱 실패", e)
        }
    }

    private suspend fun requestText(
        systemInstruction: String,
        userContent: String,
        responseSchema: GeminiSchema,
    ): String =
        withContext(Dispatchers.IO) {
            try {
                val response =
                    restClient
                        .post()
                        .uri("${geminiProperties.baseUrl}/v1beta/models/${geminiProperties.model}:generateContent")
                        .header("x-goog-api-key", geminiProperties.apiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(
                            GeminiGenerateContentRequest(
                                systemInstruction = GeminiContent(listOf(GeminiPart(systemInstruction))),
                                contents = listOf(GeminiContent(listOf(GeminiPart(userContent)))),
                                generationConfig =
                                    GeminiGenerationConfig(
                                        responseMimeType = "application/json",
                                        responseSchema = responseSchema,
                                        thinkingLevel = geminiProperties.thinkingLevel,
                                    ),
                            ),
                        ).retrieve()
                        .body(GeminiGenerateContentResponse::class.java)
                extractText(response)
            } catch (e: RestClientException) {
                throw GeminiRequestException(e)
            }
        }

    private fun extractText(response: GeminiGenerateContentResponse?): String =
        response
            ?.candidates
            ?.firstOrNull()
            ?.content
            ?.parts
            ?.firstOrNull()
            ?.text
            ?: throw GeminiResponseFormatException("Gemini 응답에 candidates/parts가 없음")

    /** [GeminiRequestException]만 재시도한다. 백오프는 취소에 반응하도록 delay()를 쓴다(Thread.sleep 금지). */
    private suspend fun <T> retryOnTransientFailure(block: suspend () -> T): T {
        var attempt = 0
        while (true) {
            try {
                return block()
            } catch (e: GeminiRequestException) {
                if (attempt >= geminiProperties.maxRetries) {
                    logger.warn(e) { "Gemini 요청 재시도 소진(${attempt}회) — 작업 전체를 실패시킨다" }
                    throw e
                }
                delay(BASE_BACKOFF_MILLIS * (1L shl attempt))
                attempt++
            }
        }
    }

    companion object {
        private const val BASE_BACKOFF_MILLIS = 500L
    }
}
