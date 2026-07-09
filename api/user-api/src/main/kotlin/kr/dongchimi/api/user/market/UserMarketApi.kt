package kr.dongchimi.api.user.market

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import kr.dongchimi.api.core.common.dto.ApiResponse
import kr.dongchimi.api.core.common.swagger.ApiErrorCodes
import kr.dongchimi.api.user.UserApiUser
import kr.dongchimi.api.user.market.response.MarketDetailResponse
import kr.dongchimi.core.common.exception.CommonErrorCode
import kr.dongchimi.core.market.MarketErrorCode
import org.springframework.web.bind.annotation.PathVariable

@Tag(name = "Market", description = "마트 정보 API")
interface UserMarketApi {
    @Operation(
        summary = "마트 상세 조회",
        description = "전단 slug로 마트 상세 정보(마트 기본 정보, 영업시간, 현재 영업중 여부, 인기 상품 TOP3)를 조회한다.",
    )
    @ApiErrorCodes(CommonErrorCode::class, MarketErrorCode::class)
    fun getDetail(
        @Parameter(hidden = true) apiUser: UserApiUser,
        @Parameter(description = "전단 slug") @PathVariable slug: String,
    ): ApiResponse<MarketDetailResponse>
}
