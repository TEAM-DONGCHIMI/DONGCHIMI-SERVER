package kr.dongchimi.core.admin

import kr.dongchimi.common.exception.ErrorStatus
import kr.dongchimi.core.common.exception.ErrorCode

enum class DefaultProductThumbnailErrorCode(
    override val status: Int,
    override val message: String,
) : ErrorCode {
    THUMBNAIL_NAME_EXISTS(ErrorStatus.CONFLICT, "이미 존재하는 기본 이미지 이름입니다."),
    THUMBNAIL_NOT_FOUND(ErrorStatus.NOT_FOUND, "존재하지 않는 기본 이미지입니다."),
}
