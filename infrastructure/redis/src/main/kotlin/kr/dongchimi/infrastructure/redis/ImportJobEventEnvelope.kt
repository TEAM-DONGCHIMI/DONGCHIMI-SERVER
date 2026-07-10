package kr.dongchimi.infrastructure.redis

import kr.dongchimi.core.product.import.ImportJobEvent
import kr.dongchimi.core.product.import.ImportJobStatus
import kr.dongchimi.core.product.import.ImportStep
import kr.dongchimi.core.product.import.ImportStepProgress

/**
 * ImportJobEvent(sealed)를 Redis pub/sub JSON으로 실어 보내기 위한 평평한(flat) 봉투.
 * core는 Jackson에 의존하지 않으므로(순수 Kotlin 원칙) 다형성 직렬화 변환은 인프라 쪽에서만 담당한다.
 */
internal data class ImportJobEventEnvelope(
    val type: String,
    val jobId: String,
    val status: ImportJobStatus? = null,
    val progress: Int? = null,
    val remainingSeconds: Int? = null,
    val currentStep: ImportStep? = null,
    val steps: List<ImportStepProgress>? = null,
    val totalCount: Int? = null,
    val successCount: Int? = null,
    val failCount: Int? = null,
    val errorCode: String? = null,
    val message: String? = null,
) {
    fun toDomain(): ImportJobEvent =
        when (type) {
            "PROGRESS" ->
                ImportJobEvent.Progress(
                    jobId = jobId,
                    status = requireNotNull(status) { "progress 이벤트에 status 필드가 없다" },
                    progress = requireNotNull(progress) { "progress 이벤트에 progress 필드가 없다" },
                    remainingSeconds = remainingSeconds,
                    currentStep = currentStep,
                    steps = requireNotNull(steps) { "progress 이벤트에 steps 필드가 없다" },
                )

            "COMPLETED" ->
                ImportJobEvent.Completed(
                    jobId = jobId,
                    totalCount = requireNotNull(totalCount) { "completed 이벤트에 totalCount 필드가 없다" },
                    successCount = requireNotNull(successCount) { "completed 이벤트에 successCount 필드가 없다" },
                    failCount = requireNotNull(failCount) { "completed 이벤트에 failCount 필드가 없다" },
                )

            "FAILED" ->
                ImportJobEvent.Failed(
                    jobId = jobId,
                    errorCode = requireNotNull(errorCode) { "failed 이벤트에 errorCode 필드가 없다" },
                    message = requireNotNull(message) { "failed 이벤트에 message 필드가 없다" },
                )

            "CANCELED" -> ImportJobEvent.Canceled(jobId = jobId)

            else -> error("알 수 없는 이벤트 타입: $type")
        }

    companion object {
        fun from(event: ImportJobEvent): ImportJobEventEnvelope =
            when (event) {
                is ImportJobEvent.Progress ->
                    ImportJobEventEnvelope(
                        type = "PROGRESS",
                        jobId = event.jobId,
                        status = event.status,
                        progress = event.progress,
                        remainingSeconds = event.remainingSeconds,
                        currentStep = event.currentStep,
                        steps = event.steps,
                    )

                is ImportJobEvent.Completed ->
                    ImportJobEventEnvelope(
                        type = "COMPLETED",
                        jobId = event.jobId,
                        totalCount = event.totalCount,
                        successCount = event.successCount,
                        failCount = event.failCount,
                    )

                is ImportJobEvent.Failed ->
                    ImportJobEventEnvelope(
                        type = "FAILED",
                        jobId = event.jobId,
                        errorCode = event.errorCode,
                        message = event.message,
                    )

                is ImportJobEvent.Canceled ->
                    ImportJobEventEnvelope(
                        type = "CANCELED",
                        jobId = event.jobId,
                    )
            }
    }
}
