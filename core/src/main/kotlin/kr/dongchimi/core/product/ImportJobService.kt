package kr.dongchimi.core.product

import kr.dongchimi.core.common.exception.CoreException
import kr.dongchimi.core.market.MarketValidator
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class ImportJobService(
    private val marketValidator: MarketValidator,
    private val importJobReader: ImportJobReader,
    private val importJobAppender: ImportJobAppender,
    private val importJobFinisher: ImportJobFinisher,
    private val importJobCancelSignal: ImportJobCancelSignal,
    private val importJobEventChannel: ImportJobEventChannel,
) {
    fun startImport(
        ownerId: Long,
        marketId: Long,
        excelObjectKey: String,
    ): ImportJob {
        marketValidator.validateOwnership(marketId, ownerId)

        val job =
            ImportJob(
                jobId = generateJobId(),
                marketId = marketId,
                ownerId = ownerId,
                excelObjectKey = excelObjectKey,
                status = ImportJobStatus.PENDING,
            )
        return importJobAppender.append(job)
    }

    fun getJob(
        ownerId: Long,
        marketId: Long,
        jobId: String,
    ): ImportJob {
        marketValidator.validateOwnership(marketId, ownerId)

        return readInMarket(jobId, marketId)
    }

    /**
     * PENDING이면 아직 아무 워커도 안 잡았으니 그 자리에서 바로 CANCELED로 전이하고 이벤트까지
     * 낸다. IN_PROGRESS면 신호만 보낸다 — 실제 전이는 워커가 체크포인트에서 한다(계획서 §2-2/§3-2).
     */
    suspend fun cancel(
        ownerId: Long,
        marketId: Long,
        jobId: String,
    ) {
        marketValidator.validateOwnership(marketId, ownerId)
        val job = readInMarket(jobId, marketId)

        if (job.status == ImportJobStatus.PENDING) {
            if (importJobFinisher.cancel(jobId)) {
                importJobEventChannel.publish(ImportJobEvent.Canceled(jobId))
            }
        } else {
            importJobCancelSignal.request(jobId)
        }
    }

    private fun readInMarket(
        jobId: String,
        marketId: Long,
    ): ImportJob {
        val job = importJobReader.read(jobId)
        if (job.marketId != marketId) throw CoreException(ImportJobErrorCode.JOB_NOT_FOUND)
        return job
    }

    private fun generateJobId(): String =
        "imp_" +
            UUID
                .randomUUID()
                .toString()
                .replace("-", "")
                .take(12)
}
