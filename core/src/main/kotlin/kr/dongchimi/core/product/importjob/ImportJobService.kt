package kr.dongchimi.core.product.importjob

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kr.dongchimi.core.common.exception.CoreException
import kr.dongchimi.core.market.MarketValidator
import kr.dongchimi.core.upload.StorageClient
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
    private val importJobProgressStore: ImportJobProgressStore,
    private val storageClient: StorageClient,
) {
    fun startImport(
        ownerId: Long,
        marketId: Long,
        excelFileUrl: String,
    ): ImportJob {
        marketValidator.validateOwnership(marketId, ownerId)
        val objectKey = storageClient.resolveObjectKey(excelFileUrl) ?: throw CoreException(ImportJobErrorCode.INVALID_EXCEL_URL)

        val job =
            ImportJob(
                jobId = generateJobId(),
                marketId = marketId,
                ownerId = ownerId,
                excelObjectKey = objectKey,
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
     * 계획서 §3-2 구독 흐름 그대로다. 종료 상태면 그 이벤트 하나로 끝나는 Flow를 돌려주고
     * (Redis를 아예 안 본다), PENDING/IN_PROGRESS면 현재 상태를 먼저 emit한 뒤 실시간
     * 이벤트 구독으로 이어붙인다 — 재구독·새로고침이 이 분기 하나로 전부 처리된다.
     */
    fun subscribeProgress(
        ownerId: Long,
        marketId: Long,
        jobId: String,
    ): Flow<ImportJobEvent> {
        marketValidator.validateOwnership(marketId, ownerId)
        val job = readInMarket(jobId, marketId)

        return when (job.status) {
            ImportJobStatus.COMPLETED -> flowOf(completedEventOf(job))
            ImportJobStatus.FAILED -> flowOf(failedEventOf(job))
            ImportJobStatus.CANCELED -> flowOf(ImportJobEvent.Canceled(job.jobId))
            ImportJobStatus.PENDING ->
                flow {
                    emit(ImportJobEvent.Progress(pendingSnapshot(job.jobId), status = ImportJobStatus.PENDING))
                    emitAll(importJobEventChannel.subscribe(jobId))
                }

            ImportJobStatus.IN_PROGRESS ->
                flow {
                    val snapshot = importJobProgressStore.find(jobId) ?: pendingSnapshot(job.jobId)
                    emit(ImportJobEvent.Progress(snapshot))
                    emitAll(importJobEventChannel.subscribe(jobId))
                }
        }
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

    private fun completedEventOf(job: ImportJob) =
        ImportJobEvent.Completed(
            jobId = job.jobId,
            totalCount = requireNotNull(job.totalCount) { "COMPLETED job에 totalCount가 없다: ${job.jobId}" },
            successCount = requireNotNull(job.successCount) { "COMPLETED job에 successCount가 없다: ${job.jobId}" },
            failCount = requireNotNull(job.failCount) { "COMPLETED job에 failCount가 없다: ${job.jobId}" },
        )

    private fun failedEventOf(job: ImportJob) =
        ImportJobEvent.Failed(
            jobId = job.jobId,
            errorCode = job.errorCode ?: ImportJobErrorCode.ANALYSIS_FAILED.name,
            message = ImportJobErrorCode.ANALYSIS_FAILED.message,
        )

    private fun pendingSnapshot(jobId: String) =
        ImportJobProgress(
            jobId = jobId,
            progress = 0,
            remainingSeconds = null,
            currentStep = null,
            steps = ImportStep.entries.map { ImportStepProgress(it, ImportStepStatus.PENDING) },
        )

    private fun generateJobId(): String =
        "imp_" +
            UUID
                .randomUUID()
                .toString()
                .replace("-", "")
                .take(12)
}
