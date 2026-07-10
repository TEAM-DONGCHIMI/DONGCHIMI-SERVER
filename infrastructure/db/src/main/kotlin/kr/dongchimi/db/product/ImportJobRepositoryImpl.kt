package kr.dongchimi.db.product

import kr.dongchimi.core.product.import.ImportJob
import kr.dongchimi.core.product.import.ImportJobRepository
import kr.dongchimi.core.product.import.ImportJobResult
import kr.dongchimi.core.product.import.ImportJobStatus
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.LocalDateTime

@Repository
class ImportJobRepositoryImpl(
    private val importJobJpaRepository: ImportJobJpaRepository,
) : ImportJobRepository {
    override fun append(job: ImportJob): ImportJob = importJobJpaRepository.save(ImportJobJpaEntity(job)).toDomain()

    override fun find(jobId: String): ImportJob? = importJobJpaRepository.findByIdOrNull(jobId)?.toDomain()

    @Transactional
    override fun claimNext(
        instanceId: String,
        lease: Duration,
        maxAttempts: Int,
    ): ImportJob? {
        val jobId = importJobJpaRepository.findClaimableJobId(maxAttempts) ?: return null

        importJobJpaRepository.updateAsClaimed(jobId, instanceId, LocalDateTime.now().plus(lease))

        return importJobJpaRepository.findByIdOrNull(jobId)?.toDomain()
    }

    override fun renewLease(
        jobId: String,
        instanceId: String,
        lease: Duration,
    ): Boolean = importJobJpaRepository.renewLease(jobId, instanceId, LocalDateTime.now().plus(lease)) > 0

    override fun releaseLeases(instanceId: String) {
        importJobJpaRepository.releaseLeases(instanceId)
    }

    override fun compareAndFinish(
        jobId: String,
        status: ImportJobStatus,
        result: ImportJobResult?,
    ): Boolean =
        importJobJpaRepository.compareAndFinish(
            jobId = jobId,
            status = status.name,
            totalCount = result?.totalCount,
            successCount = result?.successCount,
            failCount = result?.failCount,
            errorCode = result?.errorCode,
        ) > 0
}
