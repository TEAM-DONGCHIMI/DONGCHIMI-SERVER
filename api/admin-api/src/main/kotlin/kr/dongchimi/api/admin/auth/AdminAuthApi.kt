package kr.dongchimi.api.admin.auth

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletResponse
import kr.dongchimi.api.admin.auth.request.AdminLoginRequest
import kr.dongchimi.api.admin.auth.request.AdminSignupRequest
import kr.dongchimi.api.admin.auth.response.AdminAuthResponse
import kr.dongchimi.api.core.common.dto.ApiResponse
import kr.dongchimi.api.core.common.swagger.ApiErrorCodes
import kr.dongchimi.core.admin.AdminErrorCode
import kr.dongchimi.core.common.exception.CommonErrorCode

@Tag(name = "Auth", description = "관리자 인증 API")
interface AdminAuthApi {
    @Operation(
        summary = "관리자 회원가입",
        description = "이름/이메일/비밀번호 + 가입 인증 코드로 회원가입한다. 가입과 동시에 access token(바디)/refresh token(쿠키)이 발급된다.",
    )
    @ApiErrorCodes(CommonErrorCode::class, AdminErrorCode::class)
    fun signup(
        request: AdminSignupRequest,
        @Parameter(hidden = true) response: HttpServletResponse,
    ): ApiResponse<AdminAuthResponse>

    @Operation(
        summary = "관리자 로그인",
        description = "이메일/비밀번호로 로그인한다. access token은 바디, refresh token은 쿠키로 내려준다.",
    )
    @ApiErrorCodes(CommonErrorCode::class, AdminErrorCode::class)
    fun login(
        request: AdminLoginRequest,
        @Parameter(hidden = true) response: HttpServletResponse,
    ): ApiResponse<AdminAuthResponse>
}
