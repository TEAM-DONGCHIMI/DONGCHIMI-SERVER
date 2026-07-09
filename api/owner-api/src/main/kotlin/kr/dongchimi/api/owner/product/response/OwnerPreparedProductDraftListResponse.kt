package kr.dongchimi.api.owner.product.response

import io.swagger.v3.oas.annotations.media.Schema

data class OwnerPreparedProductDraftListResponse(
    @Schema(description = "마트 전체 임시저장 상품 수")
    val totalCount: Long,
    @Schema(description = "마트 전체 임시저장 중 성공(SUCCESS) 수")
    val successCount: Long,
    @Schema(description = "마트 전체 임시저장 중 실패(FAIL) 수")
    val failCount: Long,
    @Schema(description = "검색·카테고리 필터 및 페이지네이션이 적용된 임시저장 상품 목록")
    val preparedProducts: List<OwnerPreparedProductDraftResponse>,
)
