package kr.dongchimi.client.ai

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "gemini")
data class GeminiProperties(
    /** Vertex AI 프로젝트 ID. 인증은 API key가 아니라 ADC(GOOGLE_APPLICATION_CREDENTIALS 등)로 이뤄진다. */
    val project: String,
    /** Vertex 리전. Gemini 3.x는 global 엔드포인트를 권장한다. */
    val location: String = "global",
    val model: String = "gemini-3.1-flash-lite",
    /** 3.x 사고 깊이: minimal/low/medium/high. 미지정 시 모델 기본이 high(가장 느림)라 반드시 낮춰 둔다. */
    val thinkingLevel: String = "minimal",
    /** SDK HTTP 호출 타임아웃. */
    val timeout: Duration = Duration.ofSeconds(60),
)
