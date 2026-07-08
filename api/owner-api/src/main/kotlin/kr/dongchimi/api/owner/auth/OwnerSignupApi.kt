package kr.dongchimi.api.owner.auth

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import kr.dongchimi.api.core.common.dto.ApiResponse
import kr.dongchimi.api.core.common.swagger.ApiErrorCodes
import kr.dongchimi.api.owner.auth.request.OwnerSignupRequest
import kr.dongchimi.api.owner.auth.response.OwnerSignupResponse
import kr.dongchimi.core.common.exception.CommonErrorCode
import kr.dongchimi.core.owner.OwnerErrorCode

@Tag(name = "Auth", description = "점주 인증 API")
interface OwnerSignupApi {
    @Operation(
        summary = "점주 회원가입",
        description = "이메일/비밀번호 기반 점주 전용 회원가입 API",
    )
    @ApiErrorCodes(CommonErrorCode::class, OwnerErrorCode::class)
    fun signup(request: OwnerSignupRequest): ApiResponse<OwnerSignupResponse>
}
