package kr.dongchimi.api.owner.auth.request

import io.swagger.v3.oas.annotations.media.Schema
import kr.dongchimi.api.core.common.exception.validate
import kr.dongchimi.core.owner.OwnerSignupCommand

data class OwnerSignupRequest(
    @Schema(description = "이메일", example = "ddongchiim@gmail.com")
    val email: String,
    @Schema(description = "비밀번호 (6~20자, 공백·한글 불가)", example = "password123!")
    val password: String,
) {
    fun toCommand(): OwnerSignupCommand {
        validate(EMAIL_REGEX.matches(email)) { "올바르지 않은 이메일 형식입니다." }
        validate(password.length in MIN_PASSWORD_LENGTH..MAX_PASSWORD_LENGTH) { "비밀번호는 6~20자로 입력해주세요." }
        validate(password.none { it.isWhitespace() }) { "비밀번호에 공백을 포함할 수 없습니다." }
        validate(!password.contains(HANGUL_REGEX)) { "비밀번호에 한글을 포함할 수 없습니다." }

        return OwnerSignupCommand(email = email, password = password)
    }

    companion object {
        private const val MIN_PASSWORD_LENGTH = 6
        private const val MAX_PASSWORD_LENGTH = 20
        private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        private val HANGUL_REGEX = Regex("[가-힣ㄱ-ㅎㅏ-ㅣ]")
    }
}
