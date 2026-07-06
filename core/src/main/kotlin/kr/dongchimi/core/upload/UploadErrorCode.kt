package kr.dongchimi.core.upload

import kr.dongchimi.common.exception.ErrorStatus
import kr.dongchimi.core.common.exception.ErrorCode

enum class UploadErrorCode(
    override val status: Int,
    override val message: String,
) : ErrorCode {
    UNSUPPORTED_CONTENT_TYPE(ErrorStatus.BAD_REQUEST, "지원하지 않는 파일 형식입니다."),
    INVALID_UPLOAD_KEY(ErrorStatus.BAD_REQUEST, "유효하지 않은 업로드 키입니다."),
    UPLOAD_NOT_FOUND(ErrorStatus.BAD_REQUEST, "업로드된 파일을 찾을 수 없습니다. 업로드를 먼저 완료해 주세요."),
    FILE_TOO_LARGE(ErrorStatus.BAD_REQUEST, "허용된 파일 크기를 초과했습니다."),
    PRESIGN_FAILED(ErrorStatus.INTERNAL_SERVER_ERROR, "업로드 URL 발급에 실패했습니다. 다시 시도해 주세요."),
}
