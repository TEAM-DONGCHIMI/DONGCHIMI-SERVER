package kr.dongchimi.client.ai

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "gemini")
data class GeminiProperties(
    val apiKey: String,
    val model: String = "gemini-3.1-flash-lite",
    /** 3.x 사고 깊이: minimal/low/medium/high. 미지정 시 모델 기본이 high(가장 느림)라 반드시 낮춰 둔다. */
    val thinkingLevel: String = "low",
    /** SDK HTTP 호출 타임아웃. */
    val timeout: Duration = Duration.ofSeconds(60),
)
