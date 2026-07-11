package kr.dongchimi.client.kakao

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "oauth.kakao")
data class KakaoProperties(
    val userInfoUri: String,
    val tokenUri: String,
    val clientId: String,
    val redirectUri: String,
    val clientSecret: String = "",
    val connectTimeout: Duration = Duration.ofSeconds(2),
    val readTimeout: Duration = Duration.ofSeconds(5),
)
