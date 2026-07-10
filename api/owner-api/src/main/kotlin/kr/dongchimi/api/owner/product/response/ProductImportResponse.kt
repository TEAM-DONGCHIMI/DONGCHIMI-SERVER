package kr.dongchimi.api.owner.product.response

import io.swagger.v3.oas.annotations.media.Schema
import kr.dongchimi.core.product.ImportJob

data class ProductImportResponse(
    @Schema(description = "실행 작업 아이디")
    val jobId: String,
) {
    constructor(job: ImportJob) : this(jobId = job.jobId)
}
