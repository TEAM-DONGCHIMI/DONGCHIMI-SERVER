package kr.dongchimi.api.user.auth

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletResponse
import kr.dongchimi.api.core.common.dto.ApiResponse
import kr.dongchimi.api.core.common.swagger.ApiErrorCodes
import kr.dongchimi.api.user.auth.request.OAuthLoginRequest
import kr.dongchimi.api.user.auth.response.OAuthLoginResponse
import kr.dongchimi.core.auth.AuthErrorCode
import kr.dongchimi.core.common.exception.CommonErrorCode
import kr.dongchimi.core.user.UserErrorCode
import org.springframework.web.bind.annotation.PathVariable

@Tag(name = "OAuth2 Login", description = "소셜 로그인/회원가입 API")
interface OAuthLoginApi {
    @Operation(
        summary = "소셜 로그인",
        description = "소셜 제공자의 access token으로 로그인한다. 처음 로그인하는 사용자는 자동으로 회원가입 처리된다.",
    )
    @ApiErrorCodes(CommonErrorCode::class, AuthErrorCode::class, UserErrorCode::class)
    fun login(
        @Parameter(description = "소셜 로그인 제공자", example = "kakao")
        @PathVariable provider: String,
        request: OAuthLoginRequest,
        @Parameter(hidden = true) response: HttpServletResponse,
    ): ApiResponse<OAuthLoginResponse>
}
