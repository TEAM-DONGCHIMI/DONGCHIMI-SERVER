package kr.dongchimi.api.owner.auth

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletResponse
import kr.dongchimi.api.core.common.dto.ApiResponse
import kr.dongchimi.api.core.common.swagger.ApiErrorCodes
import kr.dongchimi.api.owner.auth.request.OwnerLoginRequest
import kr.dongchimi.api.owner.auth.response.OwnerLoginResponse
import kr.dongchimi.core.common.exception.CommonErrorCode
import kr.dongchimi.core.owner.OwnerErrorCode

@Tag(name = "Owner Auth", description = "사장님 인증 API")
interface OwnerLoginApi {
    @Operation(
        summary = "사장님 로그인",
        description = "이메일/비밀번호로 로그인한다. access token은 바디, refresh token은 쿠키로 내려준다.",
    )
    @ApiErrorCodes(CommonErrorCode::class, OwnerErrorCode::class)
    fun login(
        request: OwnerLoginRequest,
        @Parameter(hidden = true) response: HttpServletResponse,
    ): ApiResponse<OwnerLoginResponse>
}
