package kr.dongchimi.client.kakao

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.web.client.RestClient

@Configuration
class KakaoRestClientConfig(
    private val kakaoProperties: KakaoProperties,
) {
    @Bean
    fun kakaoRestClient(builder: RestClient.Builder): RestClient =
        builder
            .requestFactory(
                SimpleClientHttpRequestFactory().apply {
                    setConnectTimeout(kakaoProperties.connectTimeout)
                    setReadTimeout(kakaoProperties.readTimeout)
                },
            ).build()
}
