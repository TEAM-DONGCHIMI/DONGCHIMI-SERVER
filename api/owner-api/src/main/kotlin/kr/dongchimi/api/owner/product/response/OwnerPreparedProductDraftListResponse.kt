package kr.dongchimi.api.owner.product.response

import io.swagger.v3.oas.annotations.media.Schema
import kr.dongchimi.core.product.DraftStatus
import kr.dongchimi.core.product.PreparedProduct
import kr.dongchimi.core.product.PreparedProductDraftCounts
import kr.dongchimi.core.product.ProductCategory
import java.math.BigDecimal
import java.time.LocalDate

data class OwnerPreparedProductDraftListResponse(
    @Schema(description = "마트 전체 임시저장 상품 수")
    val totalCount: Long,
    @Schema(description = "마트 전체 임시저장 중 성공(SUCCESS) 수")
    val successCount: Long,
    @Schema(description = "마트 전체 임시저장 중 실패(FAIL) 수")
    val failCount: Long,
    @Schema(description = "검색·카테고리 필터 및 페이지네이션이 적용된 임시저장 상품 목록")
    val preparedProducts: List<OwnerPreparedProductDraftResponse>,
) {
    constructor(counts: PreparedProductDraftCounts, preparedProducts: List<PreparedProduct>) : this(
        totalCount = counts.totalCount,
        successCount = counts.successCount,
        failCount = counts.failCount,
        preparedProducts = preparedProducts.map { OwnerPreparedProductDraftResponse(it) },
    )

    data class OwnerPreparedProductDraftResponse(
        @Schema(description = "임시저장 상품 ID")
        val preparedProductId: Long,
        @Schema(description = "상품명 (없으면 null)")
        val name: String?,
        @Schema(description = "썸네일 이미지 URL (없으면 null)")
        val thumbnailUrl: String?,
        @Schema(description = "판매(할인) 가격 (없으면 null)")
        val discountedPrice: BigDecimal?,
        @Schema(description = "카테고리 코드 (없으면 null)")
        val category: ProductCategory?,
        @Schema(description = "홍보 문구 (없으면 null)")
        val promotionalPhrase: String?,
        @Schema(description = "할인 시작일 (없으면 null)")
        val discountStartDate: LocalDate?,
        @Schema(description = "할인 종료일 (없으면 null)")
        val discountEndDate: LocalDate?,
        @Schema(description = "SUCCESS / FAIL")
        val draftStatus: DraftStatus,
        @Schema(
            description = "실패 사유 (성공이면 null)",
            allowableValues = ["이미지 누락", "카테고리 미선택", "상품명 미입력", "판매가격 미입력", "할인기간 미설정"],
        )
        val failReason: String?,
    ) {
        constructor(preparedProduct: PreparedProduct) : this(
            preparedProductId = preparedProduct.id,
            name = preparedProduct.name,
            thumbnailUrl = preparedProduct.thumbnailUrl,
            discountedPrice = preparedProduct.price?.discountedPrice,
            category = preparedProduct.category,
            promotionalPhrase = preparedProduct.promotionalPhrase,
            discountStartDate = preparedProduct.discountPeriod?.discountStartDate,
            discountEndDate = preparedProduct.discountPeriod?.discountEndDate,
            draftStatus = preparedProduct.draftStatus,
            failReason = preparedProduct.failReason?.displayName,
        )
    }
}
