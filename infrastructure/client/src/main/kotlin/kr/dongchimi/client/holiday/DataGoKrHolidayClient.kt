package kr.dongchimi.client.holiday

import kr.dongchimi.core.holiday.HolidayClient
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.util.UriComponentsBuilder
import tools.jackson.databind.JsonNode
import tools.jackson.databind.ObjectMapper
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * 한국천문연구원 특일정보 API(getRestDeInfo)로 연도별 공휴일을 조회한다.
 * 실패(HTTP 오류·resultCode != 00·파싱 실패)는 예외로 던지고 HolidayReader가 fallback 처리한다.
 */
@Component
class DataGoKrHolidayClient(
    private val properties: HolidayApiProperties,
    @Qualifier("holidayRestClient") private val restClient: RestClient,
    private val objectMapper: ObjectMapper,
) : HolidayClient {
    override fun fetchHolidays(year: Int): Set<LocalDate> {
        // serviceKey의 +, /, = 는 URI 변수 확장 시에만 엄격 인코딩(%2B 등)되므로 queryParam에 직접 넣지 않는다.
        val uri =
            UriComponentsBuilder
                .fromUriString(properties.baseUrl)
                .path("/getRestDeInfo")
                .queryParam("serviceKey", "{serviceKey}")
                .queryParam("solYear", year)
                .queryParam("numOfRows", NUM_OF_ROWS)
                .queryParam("pageNo", 1)
                .queryParam("_type", "json")
                .encode()
                .buildAndExpand(properties.serviceKey)
                .toUri()

        val body =
            restClient
                .get()
                .uri(uri)
                .retrieve()
                .body(String::class.java)
                ?: throw IllegalStateException("공휴일 API 응답이 비어 있습니다: year=$year")

        return parseHolidays(body, year)
    }

    private fun parseHolidays(
        body: String,
        year: Int,
    ): Set<LocalDate> {
        val response = objectMapper.readTree(body).path("response")

        val resultCode = response.path("header").path("resultCode").asString()
        check(resultCode == SUCCESS_CODE) { "공휴일 API 오류 응답: year=$year, resultCode=$resultCode" }

        // items는 결과가 없으면 빈 문자열, item은 1건이면 객체·여러 건이면 배열로 내려온다.
        val item = response.path("body").path("items").path("item")
        val nodes: List<JsonNode> =
            when {
                item.isArray -> item.toList()
                item.isObject -> listOf(item)
                else -> emptyList()
            }

        return nodes
            .filter { it.path("isHoliday").asString() == "Y" }
            .map { LocalDate.parse(it.path("locdate").asString(), DateTimeFormatter.BASIC_ISO_DATE) }
            .toSet()
    }

    companion object {
        private const val SUCCESS_CODE = "00"
        private const val NUM_OF_ROWS = 100
    }
}
