package kr.dongchimi.api.owner.product.request

import io.swagger.v3.oas.annotations.media.Schema
import kr.dongchimi.api.core.common.exception.validate
import kr.dongchimi.core.product.DiscountPeriod
import java.time.LocalDate

data class ProductDiscountPeriodUpdateRequest(
    @Schema(description = "할인 시작일 (YYYY-MM-DD)", example = "2025-08-01")
    val discountStartDate: LocalDate,
    @Schema(description = "할인 종료일 (YYYY-MM-DD)", example = "2025-08-16")
    val discountEndDate: LocalDate,
    @Schema(description = "변경할 상품 id 목록", example = "[1, 2, 3]")
    val productIds: List<Long>,
) {
    fun toDiscountPeriod(): DiscountPeriod {
        validate(discountStartDate <= discountEndDate) { "올바르지 않은 기간 형식입니다." }

        return DiscountPeriod(discountStartDate, discountEndDate)
    }
}
