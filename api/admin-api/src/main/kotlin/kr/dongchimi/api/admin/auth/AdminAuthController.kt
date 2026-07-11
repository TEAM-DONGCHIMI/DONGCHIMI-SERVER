package kr.dongchimi.api.admin.auth

import kr.dongchimi.api.admin.auth.request.AdminLoginRequest
import kr.dongchimi.api.admin.auth.request.AdminSignupRequest
import kr.dongchimi.api.admin.auth.response.AdminAuthResponse
import kr.dongchimi.api.core.common.dto.ApiResponse
import kr.dongchimi.core.admin.AdminAuthService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/admin")
class AdminAuthController(
    private val adminAuthService: AdminAuthService,
) : AdminAuthApi {
    @PostMapping("/signup")
    override fun signup(
        @RequestBody request: AdminSignupRequest,
    ): ApiResponse<AdminAuthResponse> {
        val result = adminAuthService.signup(request.toCommand())
        return ApiResponse.success(AdminAuthResponse(result.accessToken))
    }

    @PostMapping("/login")
    override fun login(
        @RequestBody request: AdminLoginRequest,
    ): ApiResponse<AdminAuthResponse> {
        val result = adminAuthService.login(request.toCommand())
        return ApiResponse.success(AdminAuthResponse(result.accessToken))
    }
}
