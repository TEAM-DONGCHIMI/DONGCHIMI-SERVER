package kr.dongchimi.api.owner.auth.request

import io.swagger.v3.oas.annotations.media.Schema
import kr.dongchimi.api.core.common.exception.validate
import kr.dongchimi.common.utils.RegexPatterns.containsHangul
import kr.dongchimi.common.utils.RegexPatterns.isEmail
import kr.dongchimi.core.owner.OwnerSignupCommand

data class OwnerSignupRequest(
    @Schema(description = "이메일", example = "ddongchiim@gmail.com")
    val email: String,
    @Schema(description = "비밀번호 (6~20자, 공백·한글 불가)", example = "password123!")
    val password: String,
) {
    fun toCommand(): OwnerSignupCommand {
        validate(email.isEmail()) { "올바르지 않은 이메일 형식입니다." }
        validate(password.length in MIN_PASSWORD_LENGTH..MAX_PASSWORD_LENGTH) { "비밀번호는 6~20자로 입력해주세요." }
        validate(password.none { it.isWhitespace() }) { "비밀번호에 공백을 포함할 수 없습니다." }
        validate(!password.containsHangul()) { "비밀번호에 한글을 포함할 수 없습니다." }

        return OwnerSignupCommand(email = email, password = password)
    }

    companion object {
        private const val MIN_PASSWORD_LENGTH = 6
        private const val MAX_PASSWORD_LENGTH = 20
    }
}
