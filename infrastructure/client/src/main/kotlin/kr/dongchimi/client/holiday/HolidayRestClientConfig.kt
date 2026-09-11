package kr.dongchimi.client.holiday

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.web.client.RestClient

@Configuration
class HolidayRestClientConfig(
    private val holidayApiProperties: HolidayApiProperties,
) {
    @Bean
    fun holidayRestClient(builder: RestClient.Builder): RestClient =
        builder
            .requestFactory(
                SimpleClientHttpRequestFactory().apply {
                    setConnectTimeout(holidayApiProperties.connectTimeout)
                    setReadTimeout(holidayApiProperties.readTimeout)
                },
            ).build()
}
