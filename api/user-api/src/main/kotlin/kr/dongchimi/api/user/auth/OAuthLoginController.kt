package kr.dongchimi.api.user.auth

import kr.dongchimi.api.core.dto.ApiResponse
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
) {
    @PostMapping("/{provider}")
    fun login(
        @PathVariable provider: String,
        @RequestBody request: OAuthLoginRequest,
    ): ApiResponse<OAuthLoginResponse> {
        val accessToken = oAuthLoginService.login(request.toCommand(provider))
        return ApiResponse.success(OAuthLoginResponse(accessToken))
    }
}
