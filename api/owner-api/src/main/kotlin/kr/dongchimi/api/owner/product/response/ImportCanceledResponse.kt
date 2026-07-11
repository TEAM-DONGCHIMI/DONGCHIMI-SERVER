package kr.dongchimi.api.owner.product.response

import io.swagger.v3.oas.annotations.media.Schema
import kr.dongchimi.core.product.importjob.ImportJobEvent
import kr.dongchimi.core.product.importjob.ImportJobStatus

data class ImportCanceledResponse(
    @Schema(description = "분석 작업 id")
    val jobId: String,
    @Schema(description = "작업 상태")
    val status: ImportJobStatus,
) {
    constructor(event: ImportJobEvent.Canceled) : this(jobId = event.jobId, status = ImportJobStatus.CANCELED)
}
