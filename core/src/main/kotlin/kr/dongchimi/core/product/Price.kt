package kr.dongchimi.core.product

import java.math.BigDecimal
import java.math.RoundingMode

data class Price(
    val originalPrice: BigDecimal,
    val discountedPrice: BigDecimal,
) {
    fun discountRate(): Int =
        if (originalPrice.signum() == 0) {
            0
        } else {
            originalPrice
                .subtract(discountedPrice)
                .divide(originalPrice, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal(100))
                .toInt()
        }
}
