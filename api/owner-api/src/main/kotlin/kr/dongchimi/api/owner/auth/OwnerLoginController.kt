package kr.dongchimi.api.owner.auth

import jakarta.servlet.http.HttpServletResponse
import kr.dongchimi.api.core.auth.RefreshTokenCookieFactory
import kr.dongchimi.api.core.common.dto.ApiResponse
import kr.dongchimi.api.owner.auth.request.OwnerLoginRequest
import kr.dongchimi.api.owner.auth.response.OwnerLoginResponse
import org.springframework.http.HttpHeaders
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/owners/auth/login")
class OwnerLoginController(
    private val ownerLoginQueryFacade: OwnerLoginQueryFacade,
    private val refreshTokenCookieFactory: RefreshTokenCookieFactory,
) : OwnerLoginApi {
    @PostMapping
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
