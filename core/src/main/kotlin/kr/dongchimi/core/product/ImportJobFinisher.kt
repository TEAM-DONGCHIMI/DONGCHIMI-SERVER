package kr.dongchimi.core.product

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * 세 메서드 모두 CAS(compareAndFinish)라서 이미 종료된 job에는 아무 효과가 없다 — 취소 API와
 * 워커 완료가 경합해도 먼저 커밋된 쪽이 이긴다(계획서 #12).
 */
@Component
class ImportJobFinisher(
    private val preparedProductRepository: PreparedProductRepository,
    private val importJobRepository: ImportJobRepository,
) {
    /**
     * 마트의 기존 draft를 soft delete하고 분석 결과 전부를 새 draft로 삽입한 뒤 job을 COMPLETED로
     * 전이한다. 분석 도중에는 prepared_products에 아무것도 쓰지 않으므로(계획서 §2-1) 이 트랜잭션
     * 하나가 성공하기 전까지는 부분 데이터가 남지 않는다 — 재시도(claim 회수 후 재실행)가 안전한 이유다.
     */
    @Transactional
    fun complete(
        job: ImportJob,
        drafts: List<PreparedProduct>,
        result: ImportJobResult,
    ): Boolean {
        preparedProductRepository.softDeleteAllByMarketId(job.marketId)
        preparedProductRepository.saveAll(drafts)

        return importJobRepository.compareAndFinish(job.jobId, ImportJobStatus.COMPLETED, result)
    }

    fun fail(
        jobId: String,
        errorCode: String,
    ): Boolean = importJobRepository.compareAndFinish(jobId, ImportJobStatus.FAILED, ImportJobResult(errorCode = errorCode))

    fun cancel(jobId: String): Boolean = importJobRepository.compareAndFinish(jobId, ImportJobStatus.CANCELED, result = null)
}
