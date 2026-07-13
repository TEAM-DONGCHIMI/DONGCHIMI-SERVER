package kr.dongchimi.api.owner.product.response

import io.swagger.v3.oas.annotations.media.Schema
import kr.dongchimi.core.product.importjob.ImportJobEvent
import kr.dongchimi.core.product.importjob.ImportJobStatus

data class ImportCompletedResponse(
    @Schema(description = "분석 작업 id")
    val jobId: String,
    @Schema(description = "작업 상태")
    val status: ImportJobStatus,
    @Schema(description = "진행률(%)")
    val progress: Int,
    @Schema(description = "전체 분석 상품 수")
    val totalCount: Int,
    @Schema(description = "분석 성공(등록완료) 상품 수")
    val successCount: Int,
    @Schema(description = "분석 실패(수정필요) 상품 수")
    val failCount: Int,
) {
    constructor(event: ImportJobEvent.Completed) : this(
        jobId = event.jobId,
        status = ImportJobStatus.COMPLETED,
        progress = COMPLETED_PROGRESS,
        totalCount = event.totalCount,
        successCount = event.successCount,
        failCount = event.failCount,
    )

    companion object {
        private const val COMPLETED_PROGRESS = 100
    }
}
