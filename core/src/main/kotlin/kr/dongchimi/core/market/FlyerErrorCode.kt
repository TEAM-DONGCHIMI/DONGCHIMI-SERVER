package kr.dongchimi.core.market

import kr.dongchimi.common.exception.ErrorStatus
import kr.dongchimi.core.common.exception.ErrorCode

enum class FlyerErrorCode(
    override val status: Int,
    override val message: String,
) : ErrorCode {
    FLYER_NOT_FOUND(ErrorStatus.NOT_FOUND, "발행된 전단이 없습니다. 전단 발행을 먼저 완료해 주세요."),
    QR_GENERATION_FAILED(ErrorStatus.INTERNAL_SERVER_ERROR, "QR 코드 생성에 실패했습니다. 다시 시도해 주세요."),
}
