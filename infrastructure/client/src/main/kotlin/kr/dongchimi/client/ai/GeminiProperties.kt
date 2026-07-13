package kr.dongchimi.client.ai

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "gemini")
data class GeminiProperties(
    val apiKey: String,
    val model: String = "gemini-2.5-flash",
    val baseUrl: String = "https://generativelanguage.googleapis.com",
    val connectTimeout: Duration = Duration.ofSeconds(3),
    val readTimeout: Duration = Duration.ofSeconds(20),
    val maxRetries: Int = 2,
    /** 3.x 모델의 사고 깊이. minimal/low/medium(기본)/high. 분류·매칭은 추론이 얕아도 돼 낮출수록 빠르다. */
    val thinkingLevel: String = "low",
)
