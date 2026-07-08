package kr.dongchimi.api.owner.flyer

import kr.dongchimi.api.core.common.dto.ApiResponse
import kr.dongchimi.api.owner.OwnerApiUser
import kr.dongchimi.api.owner.flyer.response.FlyerPublishResponse
import kr.dongchimi.core.market.FlyerService
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/owners/markets/{marketId}/flyers")
class FlyerPublishController(
    private val flyerService: FlyerService,
) : FlyerPublishApi {
    @PostMapping
    override fun publish(
        apiUser: OwnerApiUser,
        @PathVariable marketId: Long,
    ): ApiResponse<FlyerPublishResponse> {
        val flyerPublish = flyerService.publish(apiUser.userId, marketId)

        return ApiResponse.success(FlyerPublishResponse(flyerPublish))
    }
}
