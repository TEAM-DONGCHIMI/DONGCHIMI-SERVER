package kr.dongchimi.api.user.flyer

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import kr.dongchimi.api.core.common.dto.ApiResponse
import kr.dongchimi.api.core.common.swagger.ApiErrorCode
import kr.dongchimi.api.user.UserApiUser
import kr.dongchimi.api.user.flyer.response.FlyerShareResponse
import kr.dongchimi.core.market.FlyerErrorCode
import kr.dongchimi.core.market.MarketErrorCode
import org.springframework.web.bind.annotation.PathVariable

@Tag(name = "Flyer", description = "전단 API")
interface UserFlyerApi {
    @Operation(
        summary = "전단 공유 정보 조회",
        description = "마트의 전단 공유 정보(마트명, slug, QR코드)를 조회한다. QR코드가 없으면 생성해 저장 후 반환한다.",
    )
    @ApiErrorCode(MarketErrorCode::class, "MARKET_NOT_FOUND")
    @ApiErrorCode(FlyerErrorCode::class, "FLYER_NOT_FOUND")
    fun getShareInfo(
        @Parameter(hidden = true) apiUser: UserApiUser,
        @Parameter(description = "마트 ID") @PathVariable marketId: Long,
    ): ApiResponse<FlyerShareResponse>
}
