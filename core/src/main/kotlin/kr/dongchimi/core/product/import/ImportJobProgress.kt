package kr.dongchimi.core.product.import

/**
 * Redis에 스냅샷으로 저장되는 진행상태. remainingSeconds/currentStep은 아직 첫 단계 진행률이
 * 나오기 전(PENDING, claim 직후)에는 추정치가 없어 null일 수 있다.
 */
data class ImportJobProgress(
    val jobId: String,
    val progress: Int,
    val remainingSeconds: Int?,
    val currentStep: ImportStep?,
    val steps: List<ImportStepProgress>,
)
