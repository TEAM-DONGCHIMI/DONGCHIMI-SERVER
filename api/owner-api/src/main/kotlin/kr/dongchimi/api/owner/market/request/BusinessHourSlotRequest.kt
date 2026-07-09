package kr.dongchimi.api.owner.market.request

import io.swagger.v3.oas.annotations.media.Schema

data class BusinessHourSlotRequest(
    @Schema(description = "요일 목록", example = "[\"MONDAY\", \"TUESDAY\"]")
    val days: List<String>?,
    @Schema(description = "영업일 여부. 휴무면 false")
    val isOpen: Boolean?,
    @Schema(description = "오픈 시각 (HH:mm). 휴무면 생략", example = "13:00")
    val open: String?,
    @Schema(description = "마감 시각 (HH:mm). 휴무면 생략", example = "18:00")
    val close: String?,
)
