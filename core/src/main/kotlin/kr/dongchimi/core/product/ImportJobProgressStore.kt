package kr.dongchimi.core.product

interface ImportJobProgressStore {
    suspend fun save(progress: ImportJobProgress)

    suspend fun find(jobId: String): ImportJobProgress?
}
