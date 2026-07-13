package kr.dongchimi.api.admin.auth

import jakarta.servlet.http.HttpServletResponse
import kr.dongchimi.api.admin.auth.request.AdminLoginRequest
import kr.dongchimi.api.admin.auth.request.AdminSignupRequest
import kr.dongchimi.api.admin.auth.response.AdminAuthResponse
import kr.dongchimi.api.core.auth.RefreshTokenCookieFactory
import kr.dongchimi.api.core.common.dto.ApiResponse
import kr.dongchimi.core.admin.AdminAuthResult
import kr.dongchimi.core.admin.AdminAuthService
import org.springframework.http.HttpHeaders
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/admin")
class AdminAuthController(
    private val adminAuthService: AdminAuthService,
    private val refreshTokenCookieFactory: RefreshTokenCookieFactory,
) : AdminAuthApi {
    @PostMapping("/signup")
    override fun signup(
        @RequestBody request: AdminSignupRequest,
        response: HttpServletResponse,
    ): ApiResponse<AdminAuthResponse> {
        val result = adminAuthService.signup(request.toCommand())
        setRefreshCookie(result, response)
        return ApiResponse.success(AdminAuthResponse(result.tokens.accessToken))
    }

    @PostMapping("/login")
    override fun login(
        @RequestBody request: AdminLoginRequest,
        response: HttpServletResponse,
    ): ApiResponse<AdminAuthResponse> {
        val result = adminAuthService.login(request.toCommand())
        setRefreshCookie(result, response)
        return ApiResponse.success(AdminAuthResponse(result.tokens.accessToken))
    }

    private fun setRefreshCookie(
        result: AdminAuthResult,
        response: HttpServletResponse,
    ) {
        val cookie =
            refreshTokenCookieFactory.create(
                result.tokens.refreshToken,
                result.tokens.refreshExpiresAt,
                persistent = result.isAutoLogin,
            )
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString())
    }
}
