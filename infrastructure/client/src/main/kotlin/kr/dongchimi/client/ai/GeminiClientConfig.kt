package kr.dongchimi.client.ai

import com.google.genai.Client
import com.google.genai.types.HttpOptions
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@ConditionalOnProperty(name = ["import.ai.provider"], havingValue = "gemini")
class GeminiClientConfig(
    private val geminiProperties: GeminiProperties,
) {
    /** Gemini Developer API(API key) 기반 SDK 클라이언트. 스레드 안전해 싱글턴으로 재사용한다. */
    @Bean
    fun genAiClient(): Client =
        Client
            .builder()
            .apiKey(geminiProperties.apiKey)
            .httpOptions(HttpOptions.builder().timeout(geminiProperties.timeout.toMillis().toInt()).build())
            .build()
}
