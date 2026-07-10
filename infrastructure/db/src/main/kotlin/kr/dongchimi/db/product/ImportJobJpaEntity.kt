package kr.dongchimi.db.product

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import kr.dongchimi.core.product.import.ImportJob
import kr.dongchimi.core.product.import.ImportJobStatus
import kr.dongchimi.db.common.BaseTimeEntity
import java.time.LocalDateTime

@Entity
@Table(name = "product_import_jobs")
class ImportJobJpaEntity(
    @Id
    @Column(name = "job_id", nullable = false, columnDefinition = "VARCHAR(32)")
    val jobId: String,
    @Column(name = "market_id", nullable = false)
    val marketId: Long,
    @Column(name = "owner_id", nullable = false)
    val ownerId: Long,
    @Column(name = "excel_object_key", nullable = false)
    val excelObjectKey: String,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: ImportJobStatus,
    @Column(name = "attempt_count", nullable = false)
    var attemptCount: Int = 0,
    @Column(name = "locked_by")
    var lockedBy: String? = null,
    @Column(name = "locked_until")
    var lockedUntil: LocalDateTime? = null,
    @Column(name = "total_count")
    var totalCount: Int? = null,
    @Column(name = "success_count")
    var successCount: Int? = null,
    @Column(name = "fail_count")
    var failCount: Int? = null,
    @Column(name = "error_code")
    var errorCode: String? = null,
) : BaseTimeEntity() {
    constructor(importJob: ImportJob) : this(
        jobId = importJob.jobId,
        marketId = importJob.marketId,
        ownerId = importJob.ownerId,
        excelObjectKey = importJob.excelObjectKey,
        status = importJob.status,
        attemptCount = importJob.attemptCount,
    )

    fun toDomain(): ImportJob =
        ImportJob(
            jobId = jobId,
            marketId = marketId,
            ownerId = ownerId,
            excelObjectKey = excelObjectKey,
            status = status,
            attemptCount = attemptCount,
            totalCount = totalCount,
            successCount = successCount,
            failCount = failCount,
            errorCode = errorCode,
        )
}
