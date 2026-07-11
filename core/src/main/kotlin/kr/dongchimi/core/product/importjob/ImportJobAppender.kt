package kr.dongchimi.core.product.importjob

import org.springframework.stereotype.Component

@Component
class ImportJobAppender(
    private val importJobRepository: ImportJobRepository,
) {
    fun append(job: ImportJob): ImportJob = importJobRepository.append(job)
}
