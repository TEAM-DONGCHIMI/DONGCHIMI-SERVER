package kr.dongchimi.api.core.auth

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletResponse
import kr.dongchimi.api.core.auth.response.ReissueTokenResponse
import kr.dongchimi.api.core.common.dto.ApiResponse
import kr.dongchimi.api.core.common.swagger.ApiErrorCode
import kr.dongchimi.core.auth.AuthErrorCode
import org.springframework.web.bind.annotation.CookieValue

@Tag(name = "Token", description = "액세스/리프레시 토큰 재발급 API")
interface TokenReissueApi {
    @Operation(
        summary = "토큰 재발급",
        description = "쿠키의 refresh token으로 access token과 refresh token을 재발급한다. owner·user 공용 엔드포인트.",
    )
    @ApiErrorCode(AuthErrorCode::class, "MISSING_REFRESH_TOKEN", "INVALID_REFRESH_TOKEN")
    fun reissue(
        @Parameter(hidden = true)
        @CookieValue("\${refresh-token.cookie.name}", required = false) refreshTokenValue: String?,
        @Parameter(hidden = true) response: HttpServletResponse,
    ): ApiResponse<ReissueTokenResponse>
}
