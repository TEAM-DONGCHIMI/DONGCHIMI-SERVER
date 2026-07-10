package kr.dongchimi.api.owner.product.response

import io.swagger.v3.oas.annotations.media.Schema
import kr.dongchimi.core.product.import.ImportJobEvent
import kr.dongchimi.core.product.import.ImportJobStatus
import kr.dongchimi.core.product.import.ImportStep
import kr.dongchimi.core.product.import.ImportStepProgress
import kr.dongchimi.core.product.import.ImportStepStatus

data class ImportProgressResponse(
    @Schema(description = "분석 작업 id")
    val jobId: String,
    @Schema(description = "작업 상태")
    val status: ImportJobStatus,
    @Schema(description = "진행률(%)")
    val progress: Int,
    @Schema(description = "예상 남은 시간(초). 추정치가 없으면 null")
    val remainingSeconds: Int?,
    @Schema(description = "현재 단계. 아직 시작 전이면 null")
    val currentStep: ImportStep?,
    @Schema(description = "단계별 상태")
    val steps: List<StepResponse>,
) {
    constructor(event: ImportJobEvent.Progress) : this(
        jobId = event.jobId,
        status = event.status,
        progress = event.progress,
        remainingSeconds = event.remainingSeconds,
        currentStep = event.currentStep,
        steps = event.steps.map { StepResponse(it) },
    )

    data class StepResponse(
        @Schema(description = "분석 단계")
        val step: ImportStep,
        @Schema(description = "단계 상태")
        val status: ImportStepStatus,
    ) {
        constructor(stepProgress: ImportStepProgress) : this(step = stepProgress.step, status = stepProgress.status)
    }
}
