package kr.dongchimi.api.user.product

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import kr.dongchimi.api.core.common.dto.ApiResponse
import kr.dongchimi.api.core.common.swagger.ApiErrorCodes
import kr.dongchimi.api.user.UserApiUser
import kr.dongchimi.api.user.product.response.DailyDealListResponse
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
    @ApiErrorCodes(CommonErrorCode::class, MarketErrorCode::class)
    fun getDailyDeals(
        @Parameter(hidden = true) apiUser: UserApiUser,
        @Parameter(description = "마트 id") @PathVariable marketId: Long,
    ): ApiResponse<DailyDealListResponse>

    @Operation(
        summary = "상품 상세 조회",
        description = "상품 단건의 상세 정보와 소속 마트 이름을 조회한다.",
    )
    @ApiErrorCodes(CommonErrorCode::class, MarketErrorCode::class, ProductErrorCode::class)
    fun getDetail(
        @Parameter(hidden = true) apiUser: UserApiUser,
        @Parameter(description = "마트 id") @PathVariable marketId: Long,
        @Parameter(description = "상품 id") @PathVariable productId: Long,
    ): ApiResponse<ProductDetailResponse>
}
