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
    /** 2.5 계열의 사고 토큰 예산. 0=완전 비활성화, -1=모델 자동(dynamic). 분류·매칭은 추론이 불필요해 0으로 최소화한다. */
    val thinkingBudget: Int = 0,
)
