package kr.dongchimi.core.product.import

/**
 * 큐의 리스(lockedBy, lockedUntil)는 [ImportJobRepository]의 claim/renew/release 내부에서만
 * 다뤄지는 영속성 관심사라 도메인 객체에 노출하지 않는다.
 *
 * totalCount/successCount/failCount/errorCode는 종료 상태(COMPLETED/FAILED)일 때만 채워진다 —
 * SSE 재구독 시 이미 종료된 job을 이 필드들만으로 completed/failed 이벤트로 재구성한다.
 */
data class ImportJob(
    val jobId: String,
    val marketId: Long,
    val ownerId: Long,
    val excelObjectKey: String,
    val status: ImportJobStatus,
    val attemptCount: Int = 0,
    val totalCount: Int? = null,
    val successCount: Int? = null,
    val failCount: Int? = null,
    val errorCode: String? = null,
)
