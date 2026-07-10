package kr.dongchimi.core.product.import

import kr.dongchimi.core.common.exception.CoreException
import org.springframework.stereotype.Component

@Component
class ImportJobReader(
    private val importJobRepository: ImportJobRepository,
) {
    fun read(jobId: String): ImportJob = importJobRepository.find(jobId) ?: throw CoreException(ImportJobErrorCode.JOB_NOT_FOUND)
}
