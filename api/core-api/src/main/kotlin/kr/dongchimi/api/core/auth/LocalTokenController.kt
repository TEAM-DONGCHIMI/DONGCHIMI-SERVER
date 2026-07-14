package kr.dongchimi.api.core.auth

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import kr.dongchimi.api.core.common.dto.ApiResponse
import kr.dongchimi.api.core.common.swagger.ApiErrorCode
import kr.dongchimi.core.auth.AuthErrorCode
import kr.dongchimi.core.auth.Role
import kr.dongchimi.core.auth.TokenProvider
import kr.dongchimi.core.common.exception.CoreException
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Local Token", description = "로컬 개발 전용 access token 발급 API")
@Profile("local")
@RestController
@RequestMapping("/v1/auth/local-token")
class LocalTokenController(
    private val tokenProvider: TokenProvider,
) {
    @Operation(summary = "로컬 access token 발급", description = "local 프로파일에서만 동작하며, 실제 로그인 없이 지정한 역할의 access token을 발급한다.")
    @ApiErrorCode(AuthErrorCode::class, "UNSUPPORTED_ROLE")
    @GetMapping
    fun issue(
        @RequestParam role: String,
        @RequestParam(defaultValue = "1") userId: Long,
    ): ApiResponse<String> {
        val parsedRole =
            runCatching { Role.valueOf(role.uppercase()) }
                .getOrElse { throw CoreException(AuthErrorCode.UNSUPPORTED_ROLE) }

        val accessToken = tokenProvider.issueAccessToken(userId, setOf(parsedRole.name))

        return ApiResponse.success(accessToken)
    }
}
