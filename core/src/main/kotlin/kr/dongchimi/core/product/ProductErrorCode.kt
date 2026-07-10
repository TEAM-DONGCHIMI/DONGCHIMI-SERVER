package kr.dongchimi.core.product

import kr.dongchimi.common.exception.ErrorStatus
import kr.dongchimi.core.common.exception.ErrorCode

enum class ProductErrorCode(
    override val status: Int,
    override val message: String,
) : ErrorCode {
    PRODUCT_NOT_FOUND(ErrorStatus.NOT_FOUND, "존재하지 않는 상품입니다."),
    DISCOUNT_NOT_ENDED(ErrorStatus.CONFLICT, "할인 기간이 종료되지 않은 상품이 포함되어 있습니다."),
    INVALID_DISCOUNT_PERIOD(ErrorStatus.BAD_REQUEST, "오늘을 포함한 기간이어야 합니다."),
    TYPE_MISMATCH(ErrorStatus.BAD_REQUEST, "상품의 판매 유형과 일치하지 않습니다."),
}
