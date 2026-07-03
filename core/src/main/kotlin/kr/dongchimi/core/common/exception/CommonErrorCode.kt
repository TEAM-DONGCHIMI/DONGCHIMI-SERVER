package kr.dongchimi.core.common.exception

import kr.dongchimi.common.exception.ErrorStatus

enum class CommonErrorCode(
    override val status: Int,
    override val message: String,
) : ErrorCode {
    INVALID_INPUT(ErrorStatus.BAD_REQUEST, "유효하지 않은 입력값입니다."),
    UNAUTHORIZED(ErrorStatus.UNAUTHORIZED, "인증이 필요합니다."),
    INTERNAL_SERVER_ERROR(ErrorStatus.INTERNAL_SERVER_ERROR, "내부 서버 오류입니다. 다시 시도해 주세요."),
}
