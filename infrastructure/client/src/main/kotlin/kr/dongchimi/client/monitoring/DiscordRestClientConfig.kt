package kr.dongchimi.client.monitoring

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.web.client.RestClient

@Configuration
class DiscordRestClientConfig(
    private val discordProperties: DiscordProperties,
) {
    @Bean
    fun discordRestClient(builder: RestClient.Builder): RestClient =
        builder
            .requestFactory(
                SimpleClientHttpRequestFactory().apply {
                    setConnectTimeout(discordProperties.connectTimeout)
                    setReadTimeout(discordProperties.readTimeout)
                },
            ).build()
}
