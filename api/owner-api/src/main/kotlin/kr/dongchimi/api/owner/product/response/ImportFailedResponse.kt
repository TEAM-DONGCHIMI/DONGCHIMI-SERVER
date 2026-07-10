package kr.dongchimi.api.owner.product.response

import io.swagger.v3.oas.annotations.media.Schema
import kr.dongchimi.core.product.ImportJobEvent
import kr.dongchimi.core.product.ImportJobStatus

data class ImportFailedResponse(
    @Schema(description = "분석 작업 id")
    val jobId: String,
    @Schema(description = "작업 상태")
    val status: ImportJobStatus,
    @Schema(description = "에러 코드")
    val code: String,
    @Schema(description = "에러 메시지")
    val message: String,
) {
    constructor(event: ImportJobEvent.Failed) : this(
        jobId = event.jobId,
        status = ImportJobStatus.FAILED,
        code = event.errorCode,
        message = event.message,
    )
}
