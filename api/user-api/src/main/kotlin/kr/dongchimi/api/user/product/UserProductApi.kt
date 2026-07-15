package kr.dongchimi.api.user.product

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import kr.dongchimi.api.core.common.dto.ApiResponse
import kr.dongchimi.api.core.common.swagger.ApiErrorCode
import kr.dongchimi.api.user.UserApiUser
import kr.dongchimi.api.user.product.request.PeriodicProductListRequest
import kr.dongchimi.api.user.product.response.DailyProductListResponse
import kr.dongchimi.api.user.product.response.PeriodicProductListResponse
import kr.dongchimi.api.user.product.response.ProductDetailResponse
import kr.dongchimi.core.common.exception.CommonErrorCode
import kr.dongchimi.core.market.MarketErrorCode
import kr.dongchimi.core.product.ProductErrorCode
import org.springframework.web.bind.annotation.PathVariable

@Tag(name = "Product", description = "상품 조회 API")
interface UserProductApi {
    @Operation(
        summary = "오늘의 특가 상품 목록 조회",
        description = "마트의 오늘의 특가(DAILY) 상품 전체를 최근 등록순으로 조회한다. 페이지네이션 없이 전체를 반환한다.",
    )
    @ApiErrorCode(MarketErrorCode::class, "MARKET_NOT_FOUND")
    fun getDailyProducts(
        @Parameter(hidden = true) apiUser: UserApiUser,
        @Parameter(description = "마트 id") @PathVariable marketId: Long,
    ): ApiResponse<DailyProductListResponse>

    @Operation(
        summary = "상품 상세 조회",
        description = "상품 단건의 상세 정보와 소속 마트 이름을 조회한다.",
    )
    @ApiErrorCode(MarketErrorCode::class, "MARKET_NOT_FOUND")
    @ApiErrorCode(ProductErrorCode::class, "PRODUCT_NOT_FOUND")
    fun getDetail(
        @Parameter(hidden = true) apiUser: UserApiUser,
        @Parameter(description = "마트 id") @PathVariable marketId: Long,
        @Parameter(description = "상품 id") @PathVariable productId: Long,
    ): ApiResponse<ProductDetailResponse>

    @Operation(
        summary = "행사 할인 상품 목록 조회",
        description =
            "마트의 기간 할인(PERIODIC) 상품을 카테고리(선택)로 필터링해 최근 등록순 커서 기반 무한스크롤로 조회한다. " +
                "현재 활성화된 상품이 존재하는 카테고리 목록을 함께 내려준다.",
    )
    @ApiErrorCode(CommonErrorCode::class, "INVALID_INPUT")
    @ApiErrorCode(MarketErrorCode::class, "MARKET_NOT_FOUND")
    fun getPeriodicDeals(
        @Parameter(hidden = true) apiUser: UserApiUser,
        @Parameter(description = "마트 id") @PathVariable marketId: Long,
        request: PeriodicProductListRequest,
    ): ApiResponse<PeriodicProductListResponse>
}
