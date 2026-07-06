package kr.dongchimi.api.user.auth

import io.swagger.v3.oas.annotations.Parameter
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
) : OAuthLoginApi {
    @PostMapping("/{provider}")
    override fun login(
        @Parameter(description = "소셜 로그인 제공자", example = "kakao")
        @PathVariable provider: String,
        @RequestBody request: OAuthLoginRequest,
    ): ApiResponse<OAuthLoginResponse> {
        val accessToken = oAuthLoginService.login(request.toCommand(provider))
        return ApiResponse.success(OAuthLoginResponse(accessToken))
    }
}
