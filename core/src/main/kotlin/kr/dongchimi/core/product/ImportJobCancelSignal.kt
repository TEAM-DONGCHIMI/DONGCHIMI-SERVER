package kr.dongchimi.core.product

interface ImportJobCancelSignal {
    /**
     * 취소를 요청한다. 플래그를 세워 워커가 다음 체크포인트에서 반드시 보게 하고,
     * 동시에 즉시성을 위한 fast-path 알림도 보낸다(워커가 그 알림을 구독해 즉시 반응하는
     * 부분은 실제로 워커를 구현하는 시점에 추가한다 — 이 인터페이스는 신호를 보내는
     * 쪽만 다룬다).
     */
    suspend fun request(jobId: String)

    suspend fun isRequested(jobId: String): Boolean
}
