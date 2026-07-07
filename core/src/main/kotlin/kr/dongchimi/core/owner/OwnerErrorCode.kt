package kr.dongchimi.core.owner

import kr.dongchimi.common.exception.ErrorStatus
import kr.dongchimi.core.common.exception.ErrorCode

enum class OwnerErrorCode(
    override val status: Int,
    override val message: String,
) : ErrorCode {
    DUPLICATE_EMAIL(ErrorStatus.CONFLICT, "이미 가입된 이메일입니다."),
}
