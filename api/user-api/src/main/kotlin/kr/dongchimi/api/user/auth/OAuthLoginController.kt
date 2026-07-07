package kr.dongchimi.api.user.auth

import jakarta.servlet.http.HttpServletResponse
import kr.dongchimi.api.core.auth.RefreshTokenCookieFactory
import kr.dongchimi.api.core.common.dto.ApiResponse
import kr.dongchimi.api.user.auth.request.OAuthLoginRequest
import kr.dongchimi.api.user.auth.response.OAuthLoginResponse
import kr.dongchimi.core.auth.OAuthLoginService
import org.springframework.http.HttpHeaders
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/users/login/oauth2")
class OAuthLoginController(
    private val oAuthLoginService: OAuthLoginService,
    private val refreshTokenCookieFactory: RefreshTokenCookieFactory,
) : OAuthLoginApi {
    @PostMapping("/{provider}")
    override fun login(
        @PathVariable provider: String,
        @RequestBody request: OAuthLoginRequest,
        response: HttpServletResponse,
    ): ApiResponse<OAuthLoginResponse> {
        val tokens = oAuthLoginService.login(request.toCommand(provider))
        response.addHeader(
            HttpHeaders.SET_COOKIE,
            refreshTokenCookieFactory.create(tokens.refreshToken, tokens.refreshExpiresAt).toString(),
        )

        return ApiResponse.success(OAuthLoginResponse(tokens.accessToken))
    }
}
