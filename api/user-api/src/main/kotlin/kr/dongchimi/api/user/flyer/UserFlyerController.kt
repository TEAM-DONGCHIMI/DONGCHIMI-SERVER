package kr.dongchimi.api.user.flyer

import kr.dongchimi.api.core.common.dto.ApiResponse
import kr.dongchimi.api.user.UserApiUser
import kr.dongchimi.api.user.flyer.response.FlyerShareResponse
import kr.dongchimi.core.market.FlyerService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/users/markets/{marketId}/flyers")
class UserFlyerController(
    private val flyerService: FlyerService,
) : UserFlyerApi {
    @GetMapping
    override fun getShareInfo(
        apiUser: UserApiUser,
        @PathVariable marketId: Long,
    ): ApiResponse<FlyerShareResponse> {
        val shareInfo = flyerService.getShareInfo(marketId)

        return ApiResponse.success(FlyerShareResponse(shareInfo))
    }
}
