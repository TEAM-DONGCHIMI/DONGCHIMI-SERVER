package kr.dongchimi.api.owner.product.request

import io.swagger.v3.oas.annotations.media.Schema
import kr.dongchimi.api.core.common.exception.validate
import kr.dongchimi.core.product.DealType
import kr.dongchimi.core.product.DiscountPeriod
import kr.dongchimi.core.product.Price
import kr.dongchimi.core.product.ProductCategory
import kr.dongchimi.core.product.ProductUpdateCommand
import java.math.BigDecimal
import java.time.LocalDate

data class ProductUpdateRequest(
    @Schema(description = "판매 유형 (상품의 기존 유형과 일치해야 함)")
    val type: DealType,
    @Schema(description = "상품 이미지 URL (미입력 시 null 저장)")
    val thumbnailUrl: String? = null,
    @Schema(description = "상품명")
    val name: String,
    @Schema(description = "상품 구분")
    val category: ProductCategory,
    @Schema(description = "상품 한줄 홍보글 (선택)")
    val promotionalPhrase: String? = null,
    @Schema(description = "정가 (DAILY만 필수, PERIODIC은 받지 않음)")
    val originalPrice: BigDecimal? = null,
    @Schema(description = "판매가")
    val discountedPrice: BigDecimal,
    @Schema(description = "행사 시작일", example = "2026-06-30")
    val discountStartDate: LocalDate,
    @Schema(description = "행사 종료일", example = "2026-06-30")
    val discountEndDate: LocalDate,
) {
    fun toCommand(): ProductUpdateCommand {
        validate(name.isNotBlank()) { "상품명은 공백일 수 없습니다." }
        validate(discountStartDate <= discountEndDate) { "올바르지 않은 기간 형식입니다." }

        return ProductUpdateCommand(
            dealType = type,
            name = name,
            thumbnailUrl = thumbnailUrl?.takeIf { it.isNotBlank() },
            price = toPrice(),
            category = category,
            promotionalPhrase = promotionalPhrase,
            discountPeriod = DiscountPeriod(discountStartDate, discountEndDate),
        )
    }

    /**
     * 가격 필드가 타입에 따라 다르다. DAILY는 정가·할인가 둘 다, PERIODIC은 판매가만 받아 원가와 동일하게 저장한다.
     */
    private fun toPrice(): Price =
        when (type) {
            DealType.DAILY -> {
                validate(originalPrice != null) { "오늘의 특가는 정가가 필요합니다." }
                val original = originalPrice!!
                validate(original.signum() >= 0 && discountedPrice.signum() >= 0) { "가격은 0원 이상이어야 합니다." }
                validate(original >= discountedPrice) { "할인가가 원가보다 큽니다." }
                Price(original, discountedPrice)
            }

            DealType.PERIODIC -> {
                validate(discountedPrice.signum() >= 0) { "가격은 0원 이상이어야 합니다." }
                Price(discountedPrice, discountedPrice)
            }
        }
}
