package kr.dongchimi.db.product

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime

interface ImportJobJpaRepository : JpaRepository<ImportJobJpaEntity, String> {
    /**
     * PENDING이거나 리스가 만료된 IN_PROGRESS 작업 중 하나를 골라 행 잠금을 건다.
     * 이 잠금은 트랜잭션이 끝날 때까지 유지되므로, 반드시 같은 트랜잭션 안에서
     * 곧바로 [updateAsClaimed]를 호출해야 한다(claimNext 하나의 원자적 동작을 두 쿼리로 나눈 것).
     */
    @Query(
        value = """
            SELECT job_id FROM product_import_jobs
            WHERE (status = 'PENDING' OR (status = 'IN_PROGRESS' AND locked_until < now()))
              AND attempt_count < :maxAttempts
            ORDER BY created_at ASC
            LIMIT 1
            FOR UPDATE SKIP LOCKED
        """,
        nativeQuery = true,
    )
    fun findClaimableJobId(
        @Param("maxAttempts") maxAttempts: Int,
    ): String?

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
        value = """
            UPDATE product_import_jobs
            SET status = 'IN_PROGRESS',
                locked_by = :instanceId,
                locked_until = :lockedUntil,
                attempt_count = attempt_count + 1,
                updated_at = now()
            WHERE job_id = :jobId
        """,
        nativeQuery = true,
    )
    fun updateAsClaimed(
        @Param("jobId") jobId: String,
        @Param("instanceId") instanceId: String,
        @Param("lockedUntil") lockedUntil: LocalDateTime,
    ): Int

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
        value = """
            UPDATE product_import_jobs
            SET locked_until = :lockedUntil,
                updated_at = now()
            WHERE job_id = :jobId
              AND locked_by = :instanceId
              AND status = 'IN_PROGRESS'
        """,
        nativeQuery = true,
    )
    fun renewLease(
        @Param("jobId") jobId: String,
        @Param("instanceId") instanceId: String,
        @Param("lockedUntil") lockedUntil: LocalDateTime,
    ): Int

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
        value = """
            UPDATE product_import_jobs
            SET status = 'PENDING',
                locked_by = null,
                locked_until = null,
                attempt_count = greatest(attempt_count - 1, 0),
                updated_at = now()
            WHERE locked_by = :instanceId
              AND status = 'IN_PROGRESS'
        """,
        nativeQuery = true,
    )
    fun releaseLeases(
        @Param("instanceId") instanceId: String,
    ): Int

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
        value = """
            UPDATE product_import_jobs
            SET status = :status,
                total_count = :totalCount,
                success_count = :successCount,
                fail_count = :failCount,
                error_code = :errorCode,
                updated_at = now()
            WHERE job_id = :jobId
              AND status NOT IN ('COMPLETED', 'FAILED', 'CANCELED')
        """,
        nativeQuery = true,
    )
    fun compareAndFinish(
        @Param("jobId") jobId: String,
        @Param("status") status: String,
        @Param("totalCount") totalCount: Int?,
        @Param("successCount") successCount: Int?,
        @Param("failCount") failCount: Int?,
        @Param("errorCode") errorCode: String?,
    ): Int
}
