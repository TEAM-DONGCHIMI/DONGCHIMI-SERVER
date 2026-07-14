package kr.dongchimi.api.owner.auth

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletResponse
import kr.dongchimi.api.core.common.dto.ApiResponse
import kr.dongchimi.api.core.common.swagger.ApiErrorCode
import kr.dongchimi.api.owner.auth.request.OwnerLoginRequest
import kr.dongchimi.api.owner.auth.request.OwnerSignupRequest
import kr.dongchimi.api.owner.auth.response.OwnerLoginResponse
import kr.dongchimi.api.owner.auth.response.OwnerSignupResponse
import kr.dongchimi.core.common.exception.CommonErrorCode
import kr.dongchimi.core.owner.OwnerErrorCode

@Tag(name = "Auth", description = "점주 인증 API")
interface OwnerAuthApi {
    @Operation(
        summary = "점주 회원가입",
        description = "이메일/비밀번호 기반 점주 전용 회원가입 API",
    )
    @ApiErrorCode(CommonErrorCode::class, "INVALID_INPUT")
    @ApiErrorCode(OwnerErrorCode::class, "DUPLICATE_EMAIL")
    fun signup(request: OwnerSignupRequest): ApiResponse<OwnerSignupResponse>

    @Operation(
        summary = "점주 로그인",
        description = "이메일/비밀번호로 로그인한다. access token은 바디, refresh token은 쿠키로 내려준다.",
    )
    @ApiErrorCode(CommonErrorCode::class, "INVALID_INPUT")
    @ApiErrorCode(OwnerErrorCode::class, "LOGIN_FAILED")
    fun login(
        request: OwnerLoginRequest,
        @Parameter(hidden = true) response: HttpServletResponse,
    ): ApiResponse<OwnerLoginResponse>
}
