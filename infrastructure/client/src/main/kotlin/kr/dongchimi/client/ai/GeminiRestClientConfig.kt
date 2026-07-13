package kr.dongchimi.client.ai

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.web.client.RestClient

@Configuration
class GeminiRestClientConfig(
    private val geminiProperties: GeminiProperties,
) {
    @Bean
    fun geminiRestClient(builder: RestClient.Builder): RestClient =
        builder
            .requestFactory(
                SimpleClientHttpRequestFactory().apply {
                    setConnectTimeout(geminiProperties.connectTimeout)
                    setReadTimeout(geminiProperties.readTimeout)
                },
            ).build()
}
