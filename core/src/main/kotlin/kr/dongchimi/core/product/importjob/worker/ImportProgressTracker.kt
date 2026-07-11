package kr.dongchimi.core.product.importjob.worker

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kr.dongchimi.core.product.importjob.ImportCanceledException
import kr.dongchimi.core.product.importjob.ImportJobCancelSignal
import kr.dongchimi.core.product.importjob.ImportJobEvent
import kr.dongchimi.core.product.importjob.ImportJobEventChannel
import kr.dongchimi.core.product.importjob.ImportJobProgress
import kr.dongchimi.core.product.importjob.ImportJobProgressStore
import kr.dongchimi.core.product.importjob.ImportStep
import kr.dongchimi.core.product.importjob.ImportStepProgress
import kr.dongchimi.core.product.importjob.ImportStepStatus

/**
 * 한 job 실행 동안의 스텝 상태(PENDING/IN_PROGRESS/COMPLETED/FAILED)와 진행률을 추적하는 per-run 헬퍼.
 * [step]으로 각 단계를 감싸면 시작 시점에 취소를 확인하고, 상태 전이와 진행률 스냅샷 저장·이벤트 발행을 대신한다.
 * remainingSeconds 추정(§9)처럼 진행률 계산이 바뀔 때 [ImportJobProcessor]를 건드리지 않고 여기만 손본다.
 */
class ImportProgressTracker(
    private val jobId: String,
    private val progressStore: ImportJobProgressStore,
    private val eventChannel: ImportJobEventChannel,
    private val cancelSignal: ImportJobCancelSignal,
) {
    private val stepStatuses = ImportStep.entries.associateWithTo(linkedMapOf()) { ImportStepStatus.PENDING }

    suspend fun <T> step(
        step: ImportStep,
        block: suspend () -> T,
    ): T {
        currentCoroutineContext().ensureActive()
        if (cancelSignal.isRequested(jobId)) throw ImportCanceledException()

        stepStatuses[step] = ImportStepStatus.IN_PROGRESS
        emit(step, 0.0)

        val result =
            try {
                block()
            } catch (e: ImportCanceledException) {
                throw e
            } catch (e: Exception) {
                stepStatuses[step] = ImportStepStatus.FAILED
                throw e
            }

        stepStatuses[step] = ImportStepStatus.COMPLETED
        emit(step, 1.0)
        return result
    }

    private suspend fun emit(
        currentStep: ImportStep,
        itemRatio: Double,
    ) {
        val snapshot =
            ImportJobProgress(
                jobId = jobId,
                progress = computeProgress(currentStep, itemRatio),
                // 최근 진행 속도 기반 추정은 §9 오픈 이슈 — 실제 AI 붙을 때 다시 손본다.
                remainingSeconds = null,
                currentStep = currentStep,
                steps = ImportStep.entries.map { ImportStepProgress(it, stepStatuses.getValue(it)) },
            )
        progressStore.save(snapshot)
        eventChannel.publish(ImportJobEvent.Progress(snapshot))
    }

    private fun computeProgress(
        currentStep: ImportStep,
        itemRatio: Double,
    ): Int {
        val steps = ImportStep.entries
        val currentIndex = steps.indexOf(currentStep)
        val completedWeight = steps.take(currentIndex).sumOf { STEP_WEIGHTS.getValue(it) }
        val currentWeight = STEP_WEIGHTS.getValue(currentStep)
        return completedWeight + (currentWeight * itemRatio).toInt()
    }

    companion object {
        /** 계획서 §3-6. 항목 수에 비례하는 두 AI 단계에 무게를 싣는다. */
        private val STEP_WEIGHTS =
            mapOf(
                ImportStep.FILE_UPLOAD to 5,
                ImportStep.NAME_EXTRACTION to 10,
                ImportStep.PRICE_EXTRACTION to 5,
                ImportStep.CATEGORY_CLASSIFICATION to 40,
                ImportStep.IMAGE_MATCHING to 40,
            )
    }
}
