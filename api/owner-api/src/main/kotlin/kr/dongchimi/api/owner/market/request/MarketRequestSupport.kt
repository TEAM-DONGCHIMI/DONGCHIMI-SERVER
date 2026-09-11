package kr.dongchimi.api.owner.market.request

import kr.dongchimi.api.core.common.exception.InvalidInputException
import kr.dongchimi.api.core.common.exception.validate
import kr.dongchimi.core.market.BusinessHourSlot
import kr.dongchimi.core.market.BusinessHours
import java.time.DayOfWeek
import java.time.LocalTime

private val BRN_REGEX = Regex("^\\d{3}-\\d{2}-\\d{5}$")
private const val NAME_MAX_LENGTH = 15
private const val DETAIL_ADDRESS_MAX_LENGTH = 20

internal fun validateMarketFields(
    name: String,
    address: String,
    detailAddress: String?,
    latitude: Double,
    longitude: Double,
    marketPhone1: String,
    marketPhone2: String?,
    marketPhonePrimary: Short,
    ownerPhone: String,
    brn: String?,
) {
    validate(name.isNotBlank()) { "마트명을 입력해 주세요." }
    validate(name.length <= NAME_MAX_LENGTH) { "마트명은 공백 포함 최대 15자까지 입력 가능합니다." }
    validate(address.isNotBlank()) { "주소를 입력해 주세요." }

    val detail = detailAddress?.takeIf { it.isNotBlank() } ?: throw InvalidInputException("상세 주소를 입력해 주세요.")
    validate(detail.length <= DETAIL_ADDRESS_MAX_LENGTH) { "상세 주소는 공백 포함 최대 20자까지 입력 가능합니다." }

    validate(latitude in -90.0..90.0) { "위도 값이 올바르지 않습니다." }
    validate(longitude in -180.0..180.0) { "경도 값이 올바르지 않습니다." }
    validate(marketPhone1.isNotBlank()) { "마트 대표 전화번호는 필수로 입력해 주세요." }
    validate(ownerPhone.isNotBlank()) { "점주 전화번호는 필수로 입력해 주세요." }
    validate(marketPhonePrimary.toInt() in 1..2) { "대표 번호 지정 값은 1 또는 2여야 합니다." }

    if (marketPhonePrimary.toInt() == 2) {
        validate(!marketPhone2.isNullOrBlank()) { "대표 번호를 2번으로 지정하려면 마트 전화번호 2를 입력해 주세요." }
    }

    brn?.let { validate(BRN_REGEX.matches(it)) { "사업자등록번호 형식이 올바르지 않습니다." } }
}

internal fun mergeAddress(
    address: String,
    detailAddress: String?,
): String = if (detailAddress.isNullOrBlank()) address else "$address|$detailAddress"

internal fun List<BusinessHourSlotRequest>?.toBusinessHours(isHolidayClosed: Boolean?): BusinessHours {
    validate(!this.isNullOrEmpty()) { "영업시간을 입력해 주세요." }

    val slots = this!!.map { it.toSlot() }

    val allDays = slots.flatMap { it.days }
    validate(allDays.isNotEmpty()) { "영업 요일을 하나 이상 선택해 주세요." }
    validate(allDays.size == allDays.toSet().size) { "같은 요일을 여러 번 지정할 수 없습니다." }

    return BusinessHours(slots = slots, isHolidayClosed = isHolidayClosed ?: false)
}

private fun BusinessHourSlotRequest.toSlot(): BusinessHourSlot {
    validate(!days.isNullOrEmpty()) { "영업 요일을 하나 이상 선택해 주세요." }
    val parsedDays = days!!.map { parseDayOfWeek(it) }
    val open = isOpen ?: throw InvalidInputException("영업일 여부를 입력해 주세요.")

    if (!open) {
        return BusinessHourSlot(days = parsedDays, isOpen = false)
    }

    validate(!this.open.isNullOrBlank()) { "영업 시작 시각을 입력해 주세요." }
    validate(!close.isNullOrBlank()) { "영업 종료 시각을 입력해 주세요." }
    val openTime = parseTime(this.open!!)
    val closeTime = parseTime(close!!)
    validate(openTime.isBefore(closeTime)) { "영업 종료 시각은 시작 시각보다 늦어야 합니다." }

    return BusinessHourSlot(days = parsedDays, isOpen = true, open = openTime, close = closeTime)
}

private fun parseDayOfWeek(value: String): DayOfWeek =
    runCatching { DayOfWeek.valueOf(value.uppercase()) }
        .getOrElse { throw InvalidInputException("요일 값이 올바르지 않습니다: $value") }

private fun parseTime(value: String): LocalTime =
    runCatching { LocalTime.parse(value) }
        .getOrElse { throw InvalidInputException("영업시간은 'HH:mm' 형식으로 입력해 주세요.") }
