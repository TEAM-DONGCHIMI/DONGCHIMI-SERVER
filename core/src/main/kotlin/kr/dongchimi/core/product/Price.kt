package kr.dongchimi.core.product

import java.math.BigDecimal

data class Price(
    val originalPrice: BigDecimal,
    val discountedPrice: BigDecimal,
) {
    fun discountRate(): Int = ((originalPrice.compareTo(discountedPrice) / originalPrice.toDouble()) * 100).toInt()
}
