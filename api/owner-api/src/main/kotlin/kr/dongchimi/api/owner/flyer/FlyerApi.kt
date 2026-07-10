package kr.dongchimi.api.owner.flyer

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import kr.dongchimi.api.core.common.dto.ApiResponse
import kr.dongchimi.api.core.common.swagger.ApiErrorCodes
import kr.dongchimi.api.owner.OwnerApiUser
import kr.dongchimi.api.owner.flyer.response.FlyerDailyPreviewResponse
import kr.dongchimi.api.owner.flyer.response.FlyerPreviewResponse
import kr.dongchimi.api.owner.flyer.response.FlyerPublishResponse
import kr.dongchimi.api.owner.flyer.response.FlyerQrResponse
import kr.dongchimi.core.common.exception.CommonErrorCode
import kr.dongchimi.core.market.FlyerErrorCode
import kr.dongchimi.core.market.MarketErrorCode
import org.springframework.web.bind.annotation.PathVariable

@Tag(name = "Flyer", description = "전단 API")
interface FlyerApi {
    @Operation(
        summary = "전단 발행",
        description = "마트의 전단을 발행하고 slug를 발급한다. 이미 발행된 경우 기존 slug를 그대로 반환한다.",
    )
    @ApiErrorCodes(CommonErrorCode::class, MarketErrorCode::class)
    fun publish(
        @Parameter(hidden = true) apiUser: OwnerApiUser,
        @Parameter(description = "마트 ID") @PathVariable marketId: Long,
    ): ApiResponse<FlyerPublishResponse>

    @Operation(
        summary = "전단 QR코드 발행",
        description = "마트의 전단 QR 코드를 발행한다. 이미 발행된 경우 저장된 QR코드를 그대로 반환한다.",
    )
    @ApiErrorCodes(CommonErrorCode::class, MarketErrorCode::class, FlyerErrorCode::class)
    fun issueQrCode(
        @Parameter(hidden = true) apiUser: OwnerApiUser,
        @Parameter(description = "마트 ID") @PathVariable marketId: Long,
    ): ApiResponse<FlyerQrResponse>

    @Operation(
        summary = "기간 할인 전단 미리보기 조회",
        description = "기간 할인 수정 후 발행 전 오늘의 전단을 최종 확인한다. 아직 발행되지 않은 임시저장(prepared_products, SUCCESS만) 상품을 포함한다.",
    )
    @ApiErrorCodes(CommonErrorCode::class, MarketErrorCode::class)
    fun getPeriodicPreview(
        @Parameter(hidden = true) apiUser: OwnerApiUser,
        @Parameter(description = "마트 ID") @PathVariable marketId: Long,
    ): ApiResponse<FlyerPreviewResponse>

    @Operation(
        summary = "오늘의 특가 전단 미리보기 조회",
        description = "오늘의 특가 수정 후 오늘의 전단을 최종 확인한다.",
    )
    @ApiErrorCodes(CommonErrorCode::class, MarketErrorCode::class)
    fun getDailyPreview(
        @Parameter(hidden = true) apiUser: OwnerApiUser,
        @Parameter(description = "마트 ID") @PathVariable marketId: Long,
    ): ApiResponse<FlyerDailyPreviewResponse>
}
