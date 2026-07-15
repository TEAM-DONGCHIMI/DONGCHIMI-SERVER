package kr.dongchimi.api.core.auth

import jakarta.servlet.http.HttpServletResponse
import kr.dongchimi.api.core.common.dto.ApiResponse
import kr.dongchimi.core.auth.LogoutService
import org.springframework.http.HttpHeaders
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/auth/token")
class LogoutController(
    private val logoutService: LogoutService,
    private val refreshTokenCookieFactory: RefreshTokenCookieFactory,
) : LogoutApi {
    @PostMapping("/logout")
    override fun logout(
        @CookieValue("\${refresh-token.cookie.name}", required = false) refreshTokenValue: String?,
        response: HttpServletResponse,
    ): ApiResponse<Unit> {
        logoutService.logout(refreshTokenValue)

        response.addHeader(
            HttpHeaders.SET_COOKIE,
            refreshTokenCookieFactory.createExpired().toString(),
        )

        return ApiResponse.success()
    }
}
