package kr.dongchimi.api.core.auth

import jakarta.servlet.http.HttpServletResponse
import kr.dongchimi.api.core.auth.response.ReissueTokenResponse
import kr.dongchimi.api.core.dto.ApiResponse
import kr.dongchimi.core.auth.AuthErrorCode
import kr.dongchimi.core.auth.ReissueTokenService
import kr.dongchimi.core.common.exception.CoreException
import org.springframework.http.HttpHeaders
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/auth/token")
class TokenReissueController(
    private val reissueTokenService: ReissueTokenService,
    private val refreshTokenCookieFactory: RefreshTokenCookieFactory,
) : TokenReissueApi {
    @PostMapping("/refresh")
    override fun reissue(
        @CookieValue("\${refresh-token.cookie.name}", required = false) refreshTokenValue: String?,
        response: HttpServletResponse,
    ): ApiResponse<ReissueTokenResponse> {
        val value = refreshTokenValue ?: throw CoreException(AuthErrorCode.MISSING_REFRESH_TOKEN)

        val tokens = reissueTokenService.reissue(value)
        response.addHeader(
            HttpHeaders.SET_COOKIE,
            refreshTokenCookieFactory.create(tokens.refreshToken, tokens.refreshExpiresAt).toString(),
        )

        return ApiResponse.success(ReissueTokenResponse(tokens.accessToken))
    }
}
