package kr.dongchimi.core.admin

import kr.dongchimi.common.exception.ErrorStatus
import kr.dongchimi.core.common.exception.ErrorCode

enum class AdminErrorCode(
    override val status: Int,
    override val message: String,
) : ErrorCode {
    ADMIN_LOGIN_FAILED(ErrorStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."),
    VERIFICATION_CODE_MISMATCH(ErrorStatus.UNAUTHORIZED, "인증코드가 일치하지 않습니다."),
    DUPLICATE_EMAIL(ErrorStatus.CONFLICT, "이미 가입된 관리자 이메일입니다."),
}
