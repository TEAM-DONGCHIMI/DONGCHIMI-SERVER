package kr.dongchimi.api.owner.product.request

import io.swagger.v3.oas.annotations.media.Schema
import kr.dongchimi.api.core.common.exception.validate
import kr.dongchimi.core.product.DailyProductRegisterCommand
import kr.dongchimi.core.product.DiscountPeriod
import kr.dongchimi.core.product.Price
import kr.dongchimi.core.product.ProductCategory
import java.math.BigDecimal
import java.time.LocalDate

data class DailyProductRegisterRequest(
    @Schema(description = "상품 이미지 URL (미입력 시 null 저장)")
    val thumbnailUrl: String? = null,
    @Schema(description = "상품명")
    val name: String,
    @Schema(description = "상품 구분")
    val category: ProductCategory,
    @Schema(description = "상품 한줄 홍보글 (선택)")
    val promotionalPhrase: String? = null,
    @Schema(description = "정가 (할인 전 가격)")
    val originalPrice: BigDecimal,
    @Schema(description = "할인가")
    val discountedPrice: BigDecimal,
    @Schema(description = "행사 시작일", example = "2026-06-30")
    val discountStartDate: LocalDate,
    @Schema(description = "행사 종료일", example = "2026-06-30")
    val discountEndDate: LocalDate,
) {
    fun toCommand(): DailyProductRegisterCommand {
        validate(name.isNotBlank()) { "상품명은 공백일 수 없습니다." }
        validate(originalPrice.signum() >= 0 && discountedPrice.signum() >= 0) { "가격은 0원 이상이어야 합니다." }
        validate(originalPrice >= discountedPrice) { "판매 가격은 정가보다 클 수 없습니다." }
        validate(discountStartDate <= discountEndDate) { "올바르지 않은 기간 형식입니다." }

        return DailyProductRegisterCommand(
            name = name,
            thumbnailUrl = thumbnailUrl?.takeIf { it.isNotBlank() },
            price = Price(originalPrice, discountedPrice),
            category = category,
            promotionalPhrase = promotionalPhrase?.takeIf { it.isNotBlank() },
            discountPeriod = DiscountPeriod(discountStartDate, discountEndDate),
        )
    }
}
