package kr.dongchimi.api.core.auth

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletResponse
import kr.dongchimi.api.core.common.dto.ApiResponse
import org.springframework.web.bind.annotation.CookieValue

@Tag(name = "Token", description = "액세스/리프레시 토큰 재발급 API")
interface LogoutApi {
    @Operation(
        summary = "로그아웃",
        description =
            "쿠키의 refresh token을 무효화하고 쿠키를 삭제한다. " +
                "쿠키가 없거나 이미 무효한 토큰이어도 항상 성공으로 응답한다(멱등). owner·admin·user 공용 엔드포인트.",
    )
    fun logout(
        @Parameter(hidden = true)
        @CookieValue("\${refresh-token.cookie.name}", required = false) refreshTokenValue: String?,
        @Parameter(hidden = true) response: HttpServletResponse,
    ): ApiResponse<Unit>
}
