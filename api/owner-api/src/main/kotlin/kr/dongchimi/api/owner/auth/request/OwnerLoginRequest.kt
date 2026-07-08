package kr.dongchimi.api.owner.auth.request

import io.swagger.v3.oas.annotations.media.Schema
import kr.dongchimi.api.core.common.exception.validate
import kr.dongchimi.common.utils.RegexPatterns.isEmail
import kr.dongchimi.core.owner.OwnerLoginCommand

data class OwnerLoginRequest(
    @Schema(description = "이메일", example = "ddongchiim@gmail.com")
    val email: String,
    @Schema(description = "비밀번호", example = "password123!")
    val password: String,
    @Schema(description = "로그인 상태 유지(자동 로그인) 여부", example = "true")
    val isAutoLogin: Boolean,
) {
    fun toCommand(): OwnerLoginCommand {
        validate(email.isEmail()) { "올바른 이메일 형식이 아닙니다." }
        validate(password.isNotBlank()) { "비밀번호를 입력해 주세요." }

        return OwnerLoginCommand(email = email, password = password, isAutoLogin = isAutoLogin)
    }
}
