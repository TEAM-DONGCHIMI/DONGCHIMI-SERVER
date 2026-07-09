package kr.dongchimi.common.utils

import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object TimeConverter {
    fun Instant.toKSTLocalDateTime(): LocalDateTime = LocalDateTime.ofInstant(this, ZoneId.of("Asia/Seoul"))

    fun LocalTime.toHHmm(): String = format(DateTimeFormatter.ofPattern("HH:mm"))
}
