package kr.dongchimi.api.owner.auth

import jakarta.servlet.http.HttpServletResponse
import kr.dongchimi.api.core.auth.RefreshTokenCookieFactory
import kr.dongchimi.api.core.common.dto.ApiResponse
import kr.dongchimi.api.owner.auth.request.OwnerLoginRequest
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
    private val refreshTokenCookieFactory: RefreshTokenCookieFactory,
) : OwnerAuthApi {
    @PostMapping("/signup")
    override fun signup(
        @RequestBody request: OwnerSignupRequest,
    ): ApiResponse<OwnerSignupResponse> {
        val owner = ownerAuthService.signup(request.toCommand())
        return ApiResponse.success(OwnerSignupResponse(ownerId = owner.id, email = owner.email))
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
