package kr.dongchimi.api.owner.flyer

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import kr.dongchimi.api.core.common.dto.ApiResponse
import kr.dongchimi.api.core.common.swagger.ApiErrorCodes
import kr.dongchimi.api.owner.OwnerApiUser
import kr.dongchimi.api.owner.flyer.response.FlyerPublishResponse
import kr.dongchimi.core.common.exception.CommonErrorCode
import kr.dongchimi.core.market.MarketErrorCode
import org.springframework.web.bind.annotation.PathVariable

@Tag(name = "Flyer", description = "전단 API")
interface FlyerPublishApi {
    @Operation(
        summary = "전단 발행",
        description = "마트의 전단을 발행하고 slug를 발급한다. 이미 발행된 경우 기존 slug를 그대로 반환한다.",
    )
    @ApiErrorCodes(CommonErrorCode::class, MarketErrorCode::class)
    fun publish(
        @Parameter(hidden = true) apiUser: OwnerApiUser,
        @Parameter(description = "마트 ID") @PathVariable marketId: Long,
    ): ApiResponse<FlyerPublishResponse>
}
