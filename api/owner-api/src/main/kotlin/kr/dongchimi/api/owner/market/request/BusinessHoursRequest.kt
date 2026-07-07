package kr.dongchimi.api.owner.market.request

import io.swagger.v3.oas.annotations.media.Schema
import kr.dongchimi.core.market.BusinessHours

data class BusinessHoursRequest(
    @Schema(description = "월요일 영업시간. 휴무면 null")
    val mon: DayScheduleRequest?,
    @Schema(description = "화요일 영업시간. 휴무면 null")
    val tue: DayScheduleRequest?,
    @Schema(description = "수요일 영업시간. 휴무면 null")
    val wed: DayScheduleRequest?,
    @Schema(description = "목요일 영업시간. 휴무면 null")
    val thu: DayScheduleRequest?,
    @Schema(description = "금요일 영업시간. 휴무면 null")
    val fri: DayScheduleRequest?,
    @Schema(description = "토요일 영업시간. 휴무면 null")
    val sat: DayScheduleRequest?,
    @Schema(description = "일요일 영업시간. 휴무면 null")
    val sun: DayScheduleRequest?,
) {
    fun toBusinessHours(): BusinessHours =
        BusinessHours(
            monday = mon?.toDaySchedule(),
            tuesday = tue?.toDaySchedule(),
            wednesday = wed?.toDaySchedule(),
            thursday = thu?.toDaySchedule(),
            friday = fri?.toDaySchedule(),
            saturday = sat?.toDaySchedule(),
            sunday = sun?.toDaySchedule(),
        )
}
