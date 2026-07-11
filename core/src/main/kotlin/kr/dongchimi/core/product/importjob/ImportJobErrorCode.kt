package kr.dongchimi.core.product.importjob

import kr.dongchimi.common.exception.ErrorStatus
import kr.dongchimi.core.common.exception.ErrorCode

enum class ImportJobErrorCode(
    override val status: Int,
    override val message: String,
) : ErrorCode {
    JOB_NOT_FOUND(ErrorStatus.NOT_FOUND, "존재하지 않는 분석 작업입니다."),
    INVALID_EXCEL_URL(ErrorStatus.BAD_REQUEST, "유효하지 않은 엑셀 파일 URL입니다."),
    ANALYSIS_FAILED(ErrorStatus.INTERNAL_SERVER_ERROR, "상품 정보 분석에 실패했습니다."),
}
