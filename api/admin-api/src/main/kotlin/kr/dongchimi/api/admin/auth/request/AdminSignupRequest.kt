package kr.dongchimi.api.admin.auth.request

import io.swagger.v3.oas.annotations.media.Schema
import kr.dongchimi.api.core.common.exception.validate
import kr.dongchimi.common.utils.RegexPatterns.containsHangul
import kr.dongchimi.common.utils.RegexPatterns.isEmail
import kr.dongchimi.core.admin.AdminSignupCommand

data class AdminSignupRequest(
    @Schema(description = "이름", example = "황수민")
    val name: String,
    @Schema(description = "이메일", example = "tnals655@dongchimi.kr")
    val email: String,
    @Schema(description = "비밀번호 (6~20자, 공백·한글 불가)", example = "password123!")
    val password: String,
    @Schema(description = "관리자 가입 인증 코드", example = "abcd1234")
    val verificationCode: String,
) {
    fun toCommand(): AdminSignupCommand {
        validate(name.isNotBlank()) { "이름을 입력해 주세요." }
        validate(email.isEmail()) { "올바르지 않은 이메일 형식입니다." }
        validate(password.length in MIN_PASSWORD_LENGTH..MAX_PASSWORD_LENGTH) { "비밀번호는 6~20자로 입력해주세요." }
        validate(password.none { it.isWhitespace() }) { "비밀번호에 공백을 포함할 수 없습니다." }
        validate(!password.containsHangul()) { "비밀번호에 한글을 포함할 수 없습니다." }
        validate(verificationCode.isNotBlank()) { "인증 코드를 입력해 주세요." }

        return AdminSignupCommand(name = name, email = email, password = password, verificationCode = verificationCode)
    }

    companion object {
        private const val MIN_PASSWORD_LENGTH = 6
        private const val MAX_PASSWORD_LENGTH = 20
    }
}
