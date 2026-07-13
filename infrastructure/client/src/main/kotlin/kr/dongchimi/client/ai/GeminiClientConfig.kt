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
    /**
     * Vertex AI 기반 SDK 클라이언트. 스레드 안전해 싱글턴으로 재사용한다.
     * 인증은 credentials를 명시하지 않고 ADC(Application Default Credentials)에 맡긴다 —
     * GOOGLE_APPLICATION_CREDENTIALS(서비스 계정 JSON), gcloud 사용자 자격증명, GCP 메타데이터 순으로 해석된다.
     */
    @Bean
    fun genAiClient(): Client =
        Client
            .builder()
            .vertexAI(true)
            .project(geminiProperties.project)
            .location(geminiProperties.location)
            .httpOptions(HttpOptions.builder().timeout(geminiProperties.timeout.toMillis().toInt()).build())
            .build()
}
