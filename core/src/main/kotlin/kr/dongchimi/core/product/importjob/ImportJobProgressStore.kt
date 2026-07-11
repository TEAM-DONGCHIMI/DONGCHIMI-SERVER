package kr.dongchimi.core.product.importjob

interface ImportJobProgressStore {
    suspend fun save(progress: ImportJobProgress)

    suspend fun find(jobId: String): ImportJobProgress?
}
