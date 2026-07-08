package kr.dongchimi.core.owner

import kr.dongchimi.common.exception.ErrorStatus
import kr.dongchimi.core.common.exception.ErrorCode

enum class OwnerErrorCode(
    override val status: Int,
    override val message: String,
) : ErrorCode {
    DUPLICATE_EMAIL(ErrorStatus.CONFLICT, "이미 가입된 이메일입니다."),
    LOGIN_FAILED(ErrorStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 일치하지 않습니다."),
}
