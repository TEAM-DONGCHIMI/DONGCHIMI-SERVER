package kr.dongchimi.core.market

import kr.dongchimi.common.exception.ErrorStatus
import kr.dongchimi.core.common.exception.ErrorCode

enum class MarketErrorCode(
    override val status: Int,
    override val message: String,
) : ErrorCode {
    MARKET_ALREADY_EXISTS(ErrorStatus.CONFLICT, "이미 존재하는 마트입니다."),
    MARKET_NOT_FOUND(ErrorStatus.NOT_FOUND, "마트를 찾을 수 없습니다."),
    MARKET_ACCESS_DENIED(ErrorStatus.FORBIDDEN, "해당 마트에 대한 권한이 없습니다."),
}
