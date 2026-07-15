package kr.dongchimi.api.owner.market

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import kr.dongchimi.api.core.common.dto.ApiResponse
import kr.dongchimi.api.core.common.swagger.ApiErrorCode
import kr.dongchimi.api.owner.OwnerApiUser
import kr.dongchimi.api.owner.market.request.MarketRegisterRequest
import kr.dongchimi.api.owner.market.request.MarketUpdateRequest
import kr.dongchimi.api.owner.market.response.MarketRegisterResponse
import kr.dongchimi.api.owner.market.response.OwnerMarketDetailResponse
import kr.dongchimi.core.common.exception.CommonErrorCode
import kr.dongchimi.core.market.MarketErrorCode
import org.springframework.web.bind.annotation.PathVariable

@Tag(name = "Market", description = "마트 정보 API")
interface OwnerMarketApi {
    @Operation(
        summary = "마트 정보 등록",
        description = "점주가 처음 접속했을 때 마트를 새로 추가하기 위해 정보를 등록한다.",
    )
    @ApiErrorCode(CommonErrorCode::class, "INVALID_INPUT")
    @ApiErrorCode(MarketErrorCode::class, "MARKET_ALREADY_EXISTS")
    fun register(
        @Parameter(hidden = true) apiUser: OwnerApiUser,
        request: MarketRegisterRequest,
    ): ApiResponse<MarketRegisterResponse>

    @Operation(
        summary = "마트 정보 상세 조회",
        description = "점주가 자신의 마트 상세 정보를 조회한다.",
    )
    @ApiErrorCode(MarketErrorCode::class, "MARKET_NOT_FOUND", "MARKET_ACCESS_DENIED")
    fun getDetail(
        @Parameter(hidden = true) apiUser: OwnerApiUser,
        @Parameter(description = "마트 ID") @PathVariable marketId: Long,
    ): ApiResponse<OwnerMarketDetailResponse>

    @Operation(
        summary = "마트 정보 수정",
        description = "점주가 자신의 마트 정보를 수정한다.",
    )
    @ApiErrorCode(CommonErrorCode::class, "INVALID_INPUT")
    @ApiErrorCode(MarketErrorCode::class, "MARKET_NOT_FOUND", "MARKET_ACCESS_DENIED", "MARKET_ALREADY_EXISTS")
    fun update(
        @Parameter(hidden = true) apiUser: OwnerApiUser,
        @Parameter(description = "마트 ID") @PathVariable marketId: Long,
        request: MarketUpdateRequest,
    ): ApiResponse<Unit>
}
