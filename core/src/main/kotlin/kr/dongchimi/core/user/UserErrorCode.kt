package kr.dongchimi.core.user

import kr.dongchimi.common.exception.ErrorStatus
import kr.dongchimi.core.common.exception.ErrorCode

enum class UserErrorCode(
    override val status: Int,
    override val message: String,
) : ErrorCode {
    DUPLICATE_SOCIAL_ACCOUNT(ErrorStatus.CONFLICT, "이미 가입된 소셜 계정입니다."),
    USER_NOT_FOUND(ErrorStatus.UNAUTHORIZED, "존재하지 않는 사용자입니다."),
}
