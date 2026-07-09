package kr.dongchimi.core.product

import java.time.Duration

interface ImportJobRepository {
    fun append(job: ImportJob): ImportJob

    fun find(jobId: String): ImportJob?

    /**
     * PENDING이거나 리스가 만료된 IN_PROGRESS 작업 중 하나에 리스를 걸고 반환한다.
     * 집을 작업이 없으면 null.
     */
    fun claimNext(
        instanceId: String,
        lease: Duration,
        maxAttempts: Int,
    ): ImportJob?

    /** 이 인스턴스가 아직 잡고 있는(status=IN_PROGRESS, locked_by=instanceId) 작업의 리스를 연장한다. */
    fun renewLease(
        jobId: String,
        instanceId: String,
        lease: Duration,
    ): Boolean

    /** 이 인스턴스가 잡고 있던 작업을 전부 PENDING으로 되돌린다(@PreDestroy에서 호출). */
    fun releaseLeases(instanceId: String)

    /**
     * 종료 상태로 CAS 전이한다. 이미 종료 상태(COMPLETED/FAILED/CANCELED)면 아무것도 바꾸지 않고 false.
     */
    fun compareAndFinish(
        jobId: String,
        status: ImportJobStatus,
        result: ImportJobResult?,
    ): Boolean
}
