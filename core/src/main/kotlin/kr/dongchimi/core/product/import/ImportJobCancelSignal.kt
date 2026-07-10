package kr.dongchimi.core.product.import

import kotlinx.coroutines.flow.Flow

interface ImportJobCancelSignal {
    /**
     * 취소를 요청한다. 플래그를 세워 워커가 다음 체크포인트에서 반드시 보게 하고,
     * 동시에 즉시성을 위한 fast-path 알림도 보낸다.
     */
    suspend fun request(jobId: String)

    suspend fun isRequested(jobId: String): Boolean

    /**
     * fast-path 알림을 구독한다. 이번 PR의 [ImportJobProcessor]는 아직 이 스트림을 레이스에
     * 붙이지 않고 체크포인트 폴링(isRequested)만으로 취소를 감지한다 — 그것만으로 정확성은
     * 이미 충분하고(플래그를 놓치지 않는 한 다음 체크포인트에서 반드시 잡힌다), 이 메서드는
     * 인스턴트 반응이 필요해지는 후속 작업을 위해 미리 완성해 둔다.
     */
    fun subscribeControl(jobId: String): Flow<Unit>
}
