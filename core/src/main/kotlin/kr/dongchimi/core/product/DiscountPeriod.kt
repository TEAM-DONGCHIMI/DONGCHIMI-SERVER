package kr.dongchimi.core.product

import java.time.LocalDate

data class DiscountPeriod(
    val discountStartDate: LocalDate,
    val discountEndDate: LocalDate,
) {
    fun isEnded(today: LocalDate): Boolean = today > discountEndDate
}
