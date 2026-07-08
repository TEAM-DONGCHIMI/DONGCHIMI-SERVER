package kr.dongchimi.api.owner.market

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import kr.dongchimi.api.core.common.dto.ApiResponse
import kr.dongchimi.api.core.common.swagger.ApiErrorCodes
import kr.dongchimi.api.owner.OwnerApiUser
import kr.dongchimi.api.owner.market.request.MarketRegisterRequest
import kr.dongchimi.api.owner.market.request.MarketUpdateRequest
import kr.dongchimi.core.common.exception.CommonErrorCode
import kr.dongchimi.core.market.MarketErrorCode
import org.springframework.web.bind.annotation.PathVariable

@Tag(name = "Market", description = "마트 정보 API")
interface MarketApi {
    @Operation(
        summary = "마트 정보 등록",
        description = "점주가 처음 접속했을 때 마트를 새로 추가하기 위해 정보를 등록한다.",
    )
    @ApiErrorCodes(CommonErrorCode::class, MarketErrorCode::class)
    fun register(
        @Parameter(hidden = true) apiUser: OwnerApiUser,
        request: MarketRegisterRequest,
    ): ApiResponse<Unit>

    @Operation(
        summary = "마트 정보 수정",
        description = "점주가 자신의 마트 정보를 수정한다.",
    )
    @ApiErrorCodes(CommonErrorCode::class, MarketErrorCode::class)
    fun update(
        @Parameter(hidden = true) apiUser: OwnerApiUser,
        @Parameter(description = "마트 ID") @PathVariable marketId: Long,
        request: MarketUpdateRequest,
    ): ApiResponse<Unit>
}
