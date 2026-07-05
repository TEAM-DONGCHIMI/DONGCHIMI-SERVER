package kr.dongchimi.core.product

import java.math.BigDecimal
import java.math.RoundingMode

data class Price(
    val originalPrice: BigDecimal,
    val discountedPrice: BigDecimal,
) {
    fun discountRate(): Int =
        originalPrice
            .subtract(discountedPrice)
            .divide(originalPrice, 4, RoundingMode.HALF_UP)
            .multiply(BigDecimal(100))
            .toInt()
}
