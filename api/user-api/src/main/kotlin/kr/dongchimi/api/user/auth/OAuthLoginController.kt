package kr.dongchimi.api.user.auth

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import kr.dongchimi.api.core.dto.ApiResponse
import kr.dongchimi.api.core.swagger.ApiErrorCodes
import kr.dongchimi.core.auth.AuthErrorCode
import kr.dongchimi.core.auth.OAuthLoginService
import kr.dongchimi.core.common.exception.CommonErrorCode
import kr.dongchimi.core.user.UserErrorCode
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "OAuth 로그인", description = "소셜 로그인/회원가입 API")
@RestController
@RequestMapping("/v1/users/login/oauth2")
class OAuthLoginController(
    private val oAuthLoginService: OAuthLoginService,
) {
    @Operation(
        summary = "소셜 로그인",
        description = "소셜 제공자의 access token으로 로그인한다. 처음 로그인하는 사용자는 자동으로 회원가입 처리된다.",
    )
    @ApiErrorCodes(CommonErrorCode::class, AuthErrorCode::class, UserErrorCode::class)
    @PostMapping("/{provider}")
    fun login(
        @Parameter(description = "소셜 로그인 제공자", example = "kakao")
        @PathVariable provider: String,
        @RequestBody request: OAuthLoginRequest,
    ): ApiResponse<OAuthLoginResponse> {
        val accessToken = oAuthLoginService.login(request.toCommand(provider))
        return ApiResponse.success(OAuthLoginResponse(accessToken))
    }
}
