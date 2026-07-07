package kr.dongchimi.api.core.auth

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletResponse
import kr.dongchimi.api.core.auth.response.ReissueTokenResponse
import kr.dongchimi.api.core.dto.ApiResponse
import kr.dongchimi.api.core.swagger.ApiErrorCodes
import kr.dongchimi.core.auth.AuthErrorCode
import kr.dongchimi.core.common.exception.CommonErrorCode
import org.springframework.web.bind.annotation.CookieValue

@Tag(name = "Token", description = "액세스/리프레시 토큰 재발급 API")
interface TokenReissueApi {
    @Operation(
        summary = "토큰 재발급",
        description = "쿠키의 refresh token으로 access token과 refresh token을 재발급한다. owner·user 공용 엔드포인트.",
    )
    @ApiErrorCodes(CommonErrorCode::class, AuthErrorCode::class)
    fun reissue(
        @Parameter(hidden = true)
        @CookieValue("\${refresh-token.cookie.name}", required = false) refreshTokenValue: String?,
        @Parameter(hidden = true) response: HttpServletResponse,
    ): ApiResponse<ReissueTokenResponse>
}
