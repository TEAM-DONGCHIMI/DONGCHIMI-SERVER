package kr.dongchimi.api.owner.auth

import jakarta.servlet.http.HttpServletResponse
import kr.dongchimi.api.core.auth.RefreshTokenCookieFactory
import kr.dongchimi.api.core.common.dto.ApiResponse
import kr.dongchimi.api.owner.auth.request.OwnerLoginRequest
import kr.dongchimi.api.owner.auth.request.OwnerSignupCompleteRequest
import kr.dongchimi.api.owner.auth.request.OwnerSignupRequest
import kr.dongchimi.api.owner.auth.response.OwnerLoginResponse
import kr.dongchimi.api.owner.auth.response.OwnerSignupResponse
import kr.dongchimi.core.owner.OwnerAuthService
import org.springframework.http.HttpHeaders
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/owners/auth")
class OwnerAuthController(
    private val ownerAuthService: OwnerAuthService,
    private val ownerLoginQueryFacade: OwnerLoginQueryFacade,
    private val ownerSignupCompletionFacade: OwnerSignupCompletionFacade,
    private val refreshTokenCookieFactory: RefreshTokenCookieFactory,
) : OwnerAuthApi {
    @PostMapping("/signup")
    override fun signup(
        @RequestBody request: OwnerSignupRequest,
    ): ApiResponse<OwnerSignupResponse> {
        val signupToken = ownerAuthService.signup(request.toCommand())
        return ApiResponse.success(OwnerSignupResponse(signupToken = signupToken, email = request.email))
    }

    @PostMapping("/signup/complete")
    override fun completeSignup(
        @RequestBody request: OwnerSignupCompleteRequest,
        response: HttpServletResponse,
    ): ApiResponse<OwnerLoginResponse> {
        val result = ownerSignupCompletionFacade.complete(request.signupToken, request.toMarketCommand())

        val cookie =
            refreshTokenCookieFactory.create(
                result.refreshToken,
                result.refreshExpiresAt,
                persistent = result.isAutoLogin,
            )
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString())

        return ApiResponse.success(result.response)
    }

    @PostMapping("/login")
    override fun login(
        @RequestBody request: OwnerLoginRequest,
        response: HttpServletResponse,
    ): ApiResponse<OwnerLoginResponse> {
        val result = ownerLoginQueryFacade.login(request.toCommand())

        val cookie =
            refreshTokenCookieFactory.create(
                result.refreshToken,
                result.refreshExpiresAt,
                persistent = result.isAutoLogin,
            )
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString())

        return ApiResponse.success(result.response)
    }
}
