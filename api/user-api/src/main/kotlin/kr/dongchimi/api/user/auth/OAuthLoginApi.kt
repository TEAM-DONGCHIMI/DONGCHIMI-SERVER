package kr.dongchimi.api.user.auth

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import kr.dongchimi.api.core.dto.ApiResponse
import kr.dongchimi.api.core.swagger.ApiErrorCodes
import kr.dongchimi.core.auth.AuthErrorCode
import kr.dongchimi.core.common.exception.CommonErrorCode
import kr.dongchimi.core.user.UserErrorCode

// 파라미터 레벨 애노테이션(@Parameter 등)은 인터페이스에서 구현체로 리플렉션 병합되지 않아 컨트롤러에 직접 붙인다
@Tag(name = "OAuth 로그인", description = "소셜 로그인/회원가입 API")
interface OAuthLoginApi {
    @Operation(
        summary = "소셜 로그인",
        description = "소셜 제공자의 access token으로 로그인한다. 처음 로그인하는 사용자는 자동으로 회원가입 처리된다.",
    )
    @ApiErrorCodes(CommonErrorCode::class, AuthErrorCode::class, UserErrorCode::class)
    fun login(
        provider: String,
        request: OAuthLoginRequest,
    ): ApiResponse<OAuthLoginResponse>
}
