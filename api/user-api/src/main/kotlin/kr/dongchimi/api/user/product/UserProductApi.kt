package kr.dongchimi.api.user.product

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import kr.dongchimi.api.core.common.dto.ApiResponse
import kr.dongchimi.api.core.common.swagger.ApiErrorCodes
import kr.dongchimi.api.user.UserApiUser
import kr.dongchimi.api.user.product.response.DailyDealListResponse
import kr.dongchimi.core.common.exception.CommonErrorCode
import kr.dongchimi.core.market.MarketErrorCode
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
}
