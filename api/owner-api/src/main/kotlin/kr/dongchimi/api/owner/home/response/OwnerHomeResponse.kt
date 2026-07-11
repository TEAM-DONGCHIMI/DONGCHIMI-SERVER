package kr.dongchimi.api.owner.home.response

import io.swagger.v3.oas.annotations.media.Schema

data class OwnerHomeResponse(
    @Schema(description = "오늘 등록한 상품 수")
    val todayRegisteredCount: Int,
    @Schema(description = "진행 중인 오늘의 특가 상품 총 개수")
    val dailyCount: Int,
    @Schema(description = "오늘의 특가 상품 미리보기 목록")
    val dailyProducts: List<HomeProductResponse>,
    @Schema(description = "진행 중인 기간 할인 상품 총 개수")
    val periodicCount: Int,
    @Schema(description = "기간 할인 상품 미리보기 목록")
    val periodicProducts: List<HomeProductResponse>,
    @Schema(description = "전단 공유 정보 (미발행 시 null)")
    val flyer: HomeFlyerResponse?,
) {
    companion object {
        val EMPTY = OwnerHomeResponse(0, 0, emptyList(), 0, emptyList(), null)
    }
}
