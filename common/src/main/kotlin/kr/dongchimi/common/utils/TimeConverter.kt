package kr.dongchimi.common.utils

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

object TimeConverter {
    fun Instant.toKSTLocalDateTime(): LocalDateTime = LocalDateTime.ofInstant(this, ZoneId.of("Asia/Seoul"))
}
