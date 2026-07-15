package kr.dongchimi.api.owner.product.request

import io.swagger.v3.oas.annotations.media.Schema
import kr.dongchimi.api.core.common.exception.validate
import kr.dongchimi.core.product.DealType
import kr.dongchimi.core.product.DiscountPeriod
import kr.dongchimi.core.product.PreparedProductDraftSaveCommand
import kr.dongchimi.core.product.Price
import kr.dongchimi.core.product.ProductCategory
import java.math.BigDecimal
import java.time.LocalDate

data class PreparedProductDraftSaveRequest(
    @Schema(description = "임시저장할 상품 목록")
    val preparedProducts: List<PreparedProductDraftRequest>,
) {
    fun toCommands(): List<PreparedProductDraftSaveCommand> {
        validate(preparedProducts.isNotEmpty()) { "임시저장할 상품이 없습니다." }
        validate(preparedProducts.distinctBy { it.preparedProductId }.size == preparedProducts.size) {
            "중복된 임시저장 상품이 포함되어 있습니다."
        }

        return preparedProducts.map { it.toCommand() }
    }

    data class PreparedProductDraftRequest(
        @Schema(description = "임시저장 상품 ID")
        val preparedProductId: Long,
        @Schema(description = "상품명 (미입력이면 null)")
        val name: String? = null,
        @Schema(description = "썸네일 이미지 URL (미입력이면 null)")
        val thumbnailUrl: String? = null,
        @Schema(description = "정가 (미입력 시 판매 가격으로 채운다)")
        val originalPrice: BigDecimal? = null,
        @Schema(description = "판매(할인) 가격 (미입력 시 정가로 채운다)")
        val discountedPrice: BigDecimal? = null,
        @Schema(description = "카테고리 코드 (미선택이면 null)")
        val category: ProductCategory? = null,
        @Schema(description = "홍보 문구 (선택)")
        val promotionalPhrase: String? = null,
        @Schema(description = "할인 시작일 (종료일과 둘 다 있을 때만 기간으로 저장)", example = "2025-08-01")
        val discountStartDate: LocalDate? = null,
        @Schema(description = "할인 종료일 (시작일과 둘 다 있을 때만 기간으로 저장)", example = "2025-08-16")
        val discountEndDate: LocalDate? = null,
        @Schema(description = "할인 유형 (미지정이면 PERIODIC)")
        val dealType: DealType? = null,
    ) {
        fun toCommand(): PreparedProductDraftSaveCommand {
            validate(preparedProductId > 0) { "올바르지 않은 임시저장 상품 ID입니다." }
            validate(name == null || name.isNotBlank()) { "상품명은 공백일 수 없습니다." }

            return PreparedProductDraftSaveCommand(
                id = preparedProductId,
                name = name,
                thumbnailUrl = thumbnailUrl,
                price = toPrice(),
                category = category,
                promotionalPhrase = promotionalPhrase,
                discountPeriod = toDiscountPeriod(),
                dealType = dealType ?: DealType.PERIODIC,
            )
        }

        /**
         * 임시저장이므로 한쪽만 입력돼도 거부하지 않는다. 빈 쪽은 반대쪽 값으로 채우고, 둘 다 없으면 저장하지 않는다.
         */
        private fun toPrice(): Price? {
            val original = originalPrice ?: discountedPrice
            val discounted = discountedPrice ?: originalPrice
            if (original == null || discounted == null) {
                return null
            }

            validate(original.signum() >= 0 && discounted.signum() >= 0) { "가격은 0원 이상이어야 합니다." }
            validate(original >= discounted) { "판매 가격은 정가보다 클 수 없습니다." }

            return Price(original, discounted)
        }

        /**
         * 임시저장이므로 한쪽 날짜만 입력돼도 거부하지 않고, 둘 다 있을 때만 기간으로 저장한다.
         */
        private fun toDiscountPeriod(): DiscountPeriod? {
            if (discountStartDate == null || discountEndDate == null) {
                return null
            }

            validate(discountStartDate <= discountEndDate) { "올바르지 않은 기간 형식입니다." }

            return DiscountPeriod(discountStartDate, discountEndDate)
        }
    }
}
