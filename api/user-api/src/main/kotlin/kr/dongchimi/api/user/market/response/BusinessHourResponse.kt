package kr.dongchimi.api.user.market.response

import io.swagger.v3.oas.annotations.media.Schema
import kr.dongchimi.common.utils.TimeConverter.toHHmm
import kr.dongchimi.core.market.BusinessHourSlot

data class BusinessHourResponse(
    @Schema(description = "요일 목록 (MONDAY ~ SUNDAY)")
    val days: List<String>,
    @Schema(description = "영업일 여부. 휴무면 false")
    val isOpen: Boolean,
    @Schema(description = "오픈 시각 (HH:mm). 휴무면 null")
    val open: String?,
    @Schema(description = "마감 시각 (HH:mm). 휴무면 null")
    val close: String?,
) {
    constructor(slot: BusinessHourSlot) : this(
        days = slot.days.map { it.name },
        isOpen = slot.isOpen,
        open = slot.open?.toHHmm(),
        close = slot.close?.toHHmm(),
    )
}
