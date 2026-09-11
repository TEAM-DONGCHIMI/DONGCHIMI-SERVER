package kr.dongchimi.api.user.market.response

import io.swagger.v3.oas.annotations.media.Schema
import kr.dongchimi.core.market.NearbyMarket
import kr.dongchimi.core.product.Product
import java.time.LocalDate
import java.time.LocalDateTime

data class NearbyMarketResponse(
    @Schema(description = "마트 id")
    val marketId: Long,
    @Schema(description = "마트명")
    val name: String,
    @Schema(description = "마트 slug")
    val slug: String,
    @Schema(description = "마트 썸네일 이미지 URL")
    val thumbnailUrl: String?,
    @Schema(description = "마트 기본 주소")
    val address: String,
    @Schema(description = "위도")
    val latitude: Double,
    @Schema(description = "경도")
    val longitude: Double,
    @Schema(description = "현재 영업중 여부")
    val isOpen: Boolean,
    @Schema(description = "진행 중인 할인 상품 개수")
    val productCount: Int,
    @Schema(description = "미리보기 상품 목록 (최신순 3개)")
    val previewProducts: List<PreviewProductResponse>,
) {
    constructor(
        nearbyMarket: NearbyMarket,
        productCount: Int,
        previewProducts: List<Product>,
        now: LocalDateTime,
        holidays: Set<LocalDate>,
    ) : this(
        marketId = nearbyMarket.market.id,
        name = nearbyMarket.market.info.name,
        slug = nearbyMarket.slug,
        thumbnailUrl = nearbyMarket.market.info.thumbnailUrl,
        address =
            nearbyMarket.market.info.address
                .substringBefore("|"),
        latitude = nearbyMarket.market.location.latitude,
        longitude = nearbyMarket.market.location.longitude,
        isOpen = nearbyMarket.market.businessHours.isOpenAt(now, holidays),
        productCount = productCount,
        previewProducts = previewProducts.map { PreviewProductResponse(it) },
    )
}
