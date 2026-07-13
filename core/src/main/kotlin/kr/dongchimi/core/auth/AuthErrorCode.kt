package kr.dongchimi.core.auth

import kr.dongchimi.common.exception.ErrorStatus
import kr.dongchimi.core.common.exception.ErrorCode

enum class AuthErrorCode(
    override val status: Int,
    override val message: String,
) : ErrorCode {
    UNSUPPORTED_OAUTH_PROVIDER(ErrorStatus.BAD_REQUEST, "지원하지 않는 소셜 로그인 제공자입니다."),
    OAUTH_AUTHENTICATION_FAILED(ErrorStatus.UNAUTHORIZED, "소셜 로그인 인증에 실패했습니다."),
    OAUTH_REQUIRED_INFO_MISSING(ErrorStatus.BAD_REQUEST, "소셜 계정에서 필수 정보(이메일)를 가져올 수 없습니다."),
    INVALID_REFRESH_TOKEN(ErrorStatus.UNAUTHORIZED, "유효하지 않은 리프레시 토큰입니다."),
    MISSING_REFRESH_TOKEN(ErrorStatus.UNAUTHORIZED, "리프레시 토큰이 없습니다."),
    UNSUPPORTED_ROLE(ErrorStatus.BAD_REQUEST, "지원하지 않는 역할입니다."),
}
