package kr.dongchimi.client.kakao

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "oauth.kakao")
data class KakaoProperties(
    val userInfoUri: String,
)
