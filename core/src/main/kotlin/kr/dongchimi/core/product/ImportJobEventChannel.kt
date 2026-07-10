package kr.dongchimi.core.product

import kotlinx.coroutines.flow.Flow

interface ImportJobEventChannel {
    suspend fun publish(event: ImportJobEvent)

    fun subscribe(jobId: String): Flow<ImportJobEvent>
}
