package kr.dongchimi.client.holiday

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "holiday.api")
data class HolidayApiProperties(
    val baseUrl: String = "https://apis.data.go.kr/B090041/openapi/service/SpcdeInfoService",
    /** data.go.kr에서 발급받은 "Decoding" 인증키 (URI 빌더가 인코딩하므로 Encoding 키를 쓰면 이중 인코딩됨) */
    val serviceKey: String = "",
    val connectTimeout: Duration = Duration.ofSeconds(2),
    val readTimeout: Duration = Duration.ofSeconds(3),
)
