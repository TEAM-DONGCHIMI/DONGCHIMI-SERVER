package kr.dongchimi.api.user.product.response

import io.swagger.v3.oas.annotations.media.Schema

data class PeriodicProductListResponse(
    @Schema(description = "조회 결과 목록")
    val content: List<PeriodicProductResponse>,
    @Schema(description = "다음 페이지 존재 여부")
    val hasNext: Boolean,
    @Schema(description = "다음 페이지 조회용 커서. 없으면 null")
    val nextCursor: Long? = null,
    @Schema(description = "해당 마트에 현재 활성화된 PERIODIC 상품이 존재하는 카테고리 목록")
    val availableCategories: List<String>,
)
