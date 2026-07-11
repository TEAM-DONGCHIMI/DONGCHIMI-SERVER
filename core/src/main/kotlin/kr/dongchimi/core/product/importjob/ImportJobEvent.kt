package kr.dongchimi.core.product.importjob

/**
 * SSE로 그대로 나가는 이벤트. Progress는 [ImportJobProgress] 스냅샷과 필드가 같아서
 * 보조 생성자로 변환한다 — 워커가 스냅샷을 저장하는 시점에 같은 값으로 이벤트도 만들고,
 * SSE 구독 시 Redis에서 읽은 스냅샷을 첫 이벤트로 재구성할 때도 이 생성자를 쓴다.
 */
sealed class ImportJobEvent {
    abstract val jobId: String

    data class Progress(
        override val jobId: String,
        val progress: Int,
        val remainingSeconds: Int?,
        val currentStep: ImportStep?,
        val steps: List<ImportStepProgress>,
    ) : ImportJobEvent() {
        constructor(snapshot: ImportJobProgress) : this(
            jobId = snapshot.jobId,
            progress = snapshot.progress,
            remainingSeconds = snapshot.remainingSeconds,
            currentStep = snapshot.currentStep,
            steps = snapshot.steps,
        )
    }

    data class Completed(
        override val jobId: String,
        val totalCount: Int,
        val successCount: Int,
        val failCount: Int,
    ) : ImportJobEvent()

    data class Failed(
        override val jobId: String,
        val errorCode: String,
        val message: String,
    ) : ImportJobEvent()

    data class Canceled(
        override val jobId: String,
    ) : ImportJobEvent()
}
