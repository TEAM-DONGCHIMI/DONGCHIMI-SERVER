package kr.dongchimi.api.owner.flyer

import kr.dongchimi.api.core.common.dto.ApiResponse
import kr.dongchimi.api.owner.OwnerApiUser
import kr.dongchimi.api.owner.flyer.response.FlyerDailyPreviewResponse
import kr.dongchimi.api.owner.flyer.response.FlyerPreviewResponse
import kr.dongchimi.api.owner.flyer.response.FlyerPublishResponse
import kr.dongchimi.api.owner.flyer.response.FlyerQrResponse
import kr.dongchimi.core.market.FlyerService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

@RestController
@RequestMapping("/v1/owners/markets/{marketId}/flyers")
class FlyerController(
    private val flyerService: FlyerService,
    private val flyerPreviewQueryFacade: FlyerPreviewQueryFacade,
) : FlyerApi {
    @PostMapping
    override fun publish(
        apiUser: OwnerApiUser,
        @PathVariable marketId: Long,
    ): ApiResponse<FlyerPublishResponse> {
        val slug = flyerService.publish(apiUser.userId, marketId)

        return ApiResponse.success(FlyerPublishResponse(slug))
    }

    @PostMapping("/qr")
    override fun issueQrCode(
        apiUser: OwnerApiUser,
        @PathVariable marketId: Long,
    ): ApiResponse<FlyerQrResponse> {
        val flyerQr = flyerService.issueQrCode(apiUser.userId, marketId)

        return ApiResponse.success(FlyerQrResponse(flyerQr))
    }

    @GetMapping("/preview/periodic")
    override fun getPeriodicPreview(
        apiUser: OwnerApiUser,
        @PathVariable marketId: Long,
    ): ApiResponse<FlyerPreviewResponse> {
        val response = flyerPreviewQueryFacade.getPeriodicPreview(apiUser.userId, marketId, LocalDateTime.now())

        return ApiResponse.success(response)
    }

    @GetMapping("/preview/daily")
    override fun getDailyPreview(
        apiUser: OwnerApiUser,
        @PathVariable marketId: Long,
    ): ApiResponse<FlyerDailyPreviewResponse> {
        val response = flyerPreviewQueryFacade.getDailyPreview(apiUser.userId, marketId, LocalDateTime.now())

        return ApiResponse.success(response)
    }
}
