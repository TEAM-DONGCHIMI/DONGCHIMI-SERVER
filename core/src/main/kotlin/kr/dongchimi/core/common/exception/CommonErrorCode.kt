package kr.dongchimi.core.common.exception

import kr.dongchimi.common.exception.ErrorStatus

enum class CommonErrorCode(
    override val status: Int,
    override val message: String,
) : ErrorCode {
    INVALID_INPUT(ErrorStatus.BAD_REQUEST, "유효하지 않은 입력값입니다."),
    UNAUTHORIZED(ErrorStatus.UNAUTHORIZED, "인증이 필요합니다."),
    FORBIDDEN(ErrorStatus.FORBIDDEN, "접근 권한이 없습니다."),
    INTERNAL_SERVER_ERROR(ErrorStatus.INTERNAL_SERVER_ERROR, "내부 서버 오류입니다. 다시 시도해 주세요."),
    RESOURCE_NOT_FOUND(ErrorStatus.NOT_FOUND, "요청한 리소스를 찾을 수 없습니다."),
    METHOD_NOT_ALLOWED(ErrorStatus.METHOD_NOT_ALLOWED, "지원하지 않는 HTTP 메서드입니다."),
    UNSUPPORTED_MEDIA_TYPE(ErrorStatus.UNSUPPORTED_MEDIA_TYPE, "지원하지 않는 미디어 타입입니다."),
    INVALID_REQUEST_BODY(ErrorStatus.BAD_REQUEST, "요청 본문을 읽을 수 없습니다."),
    MISSING_REQUEST_PARAMETER(ErrorStatus.BAD_REQUEST, "필수 파라미터가 누락되었습니다."),
    MISSING_REQUEST_HEADER(ErrorStatus.BAD_REQUEST, "필수 헤더가 누락되었습니다."),
    TYPE_MISMATCH(ErrorStatus.BAD_REQUEST, "파라미터 타입이 올바르지 않습니다."),
}
