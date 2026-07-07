package kr.dongchimi.api.owner.market.request

import io.swagger.v3.oas.annotations.media.Schema
import kr.dongchimi.api.core.common.exception.InvalidInputException
import kr.dongchimi.api.core.common.exception.validate
import kr.dongchimi.core.market.DaySchedule
import java.time.LocalTime

data class DayScheduleRequest(
    @Schema(description = "오픈 시각 (HH:mm)", example = "09:00")
    val open: String?,
    @Schema(description = "마감 시각 (HH:mm)", example = "18:00")
    val close: String?,
) {
    fun toDaySchedule(): DaySchedule {
        open?.let { validate(it.isNotBlank()) { "영업 시작 시각은 필수로 입력해 주세요." } }
        close?.let { validate(it.isNotBlank()) { "영업 종료 시각은 필수로 입력해 주세요." } }

        return DaySchedule(openTime = parseTime(open!!), closeTime = parseTime(close!!))
    }

    private fun parseTime(value: String): LocalTime =
        runCatching { LocalTime.parse(value) }
            .getOrElse { throw InvalidInputException("영업시간은 'HH:mm' 형식으로 입력해 주세요.") }
}
