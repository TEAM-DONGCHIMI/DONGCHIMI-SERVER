package kr.dongchimi.core.product

import kr.dongchimi.common.exception.ErrorStatus
import kr.dongchimi.core.common.exception.ErrorCode

enum class PreparedProductErrorCode(
    override val status: Int,
    override val message: String,
) : ErrorCode {
    PREPARED_PRODUCT_NOT_FOUND(ErrorStatus.NOT_FOUND, "존재하지 않는 임시저장 상품입니다."),
    DRAFT_NOT_COMPLETED(ErrorStatus.CONFLICT, "등록에 실패한 임시저장 상품이 포함되어 있습니다."),
}
