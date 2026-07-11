package kr.dongchimi.core.admin

import kr.dongchimi.common.exception.ErrorStatus
import kr.dongchimi.core.common.exception.ErrorCode

enum class AdminErrorCode(
    override val status: Int,
    override val message: String,
) : ErrorCode {
    ADMIN_NOT_FOUND(ErrorStatus.NOT_FOUND, "접속할 수 없는 이메일입니다."),
    ADMIN_PASSWORD_MISMATCH(ErrorStatus.UNAUTHORIZED, "비밀번호가 올바르지 않습니다."),
    VERIFICATION_CODE_MISMATCH(ErrorStatus.UNAUTHORIZED, "인증코드가 일치하지 않습니다."),
    DUPLICATE_EMAIL(ErrorStatus.CONFLICT, "이미 가입된 관리자 이메일입니다."),
}
