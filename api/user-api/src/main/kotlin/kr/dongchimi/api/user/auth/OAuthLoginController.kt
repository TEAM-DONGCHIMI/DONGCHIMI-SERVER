package kr.dongchimi.api.user.auth

import kr.dongchimi.api.core.common.dto.ApiResponse
import kr.dongchimi.api.user.auth.request.OAuthLoginRequest
import kr.dongchimi.api.user.auth.response.OAuthLoginResponse
import kr.dongchimi.core.auth.OAuthLoginService
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/users/login/oauth2")
class OAuthLoginController(
    private val oAuthLoginService: OAuthLoginService,
) : OAuthLoginApi {
    @PostMapping("/{provider}")
    override fun login(
        @PathVariable provider: String,
        @RequestBody request: OAuthLoginRequest,
    ): ApiResponse<OAuthLoginResponse> {
        val accessToken = oAuthLoginService.login(request.toCommand(provider))
        return ApiResponse.success(OAuthLoginResponse(accessToken))
    }
}
