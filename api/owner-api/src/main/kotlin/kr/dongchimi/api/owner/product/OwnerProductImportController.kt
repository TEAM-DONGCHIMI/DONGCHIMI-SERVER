package kr.dongchimi.api.owner.product

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PreDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.transformWhile
import kotlinx.coroutines.launch
import kotlinx.coroutines.slf4j.MDCContext
import kr.dongchimi.api.core.common.dto.ApiResponse
import kr.dongchimi.api.owner.OwnerApiUser
import kr.dongchimi.api.owner.product.request.ProductImportRequest
import kr.dongchimi.api.owner.product.response.ImportCanceledResponse
import kr.dongchimi.api.owner.product.response.ImportCompletedResponse
import kr.dongchimi.api.owner.product.response.ImportFailedResponse
import kr.dongchimi.api.owner.product.response.ImportProgressResponse
import kr.dongchimi.api.owner.product.response.ProductImportResponse
import kr.dongchimi.core.product.importjob.ImportJobEvent
import kr.dongchimi.core.product.importjob.ImportJobService
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.time.Duration

private val logger = KotlinLogging.logger {}

@RestController
@RequestMapping("/v1/owners/markets/{marketId}/products/import")
class OwnerProductImportController(
    private val importJobService: ImportJobService,
) : OwnerProductImportApi {
    /** SSE 스트림을 Flow와 붙여두는 동안 요청 스레드를 막지 않으려고 별도 스코프에서 collect한다. */
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @PostMapping
    override fun startImport(
        apiUser: OwnerApiUser,
        @PathVariable marketId: Long,
        @RequestBody request: ProductImportRequest,
    ): ApiResponse<ProductImportResponse> {
        val command = request.toCommand()
        val job = importJobService.startImport(apiUser.userId, marketId, command.excelFileUrl)

        return ApiResponse.success(ProductImportResponse(job))
    }

    @GetMapping("/{jobId}/progress", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    override fun subscribeProgress(
        apiUser: OwnerApiUser,
        @PathVariable marketId: Long,
        @PathVariable jobId: String,
    ): SseEmitter {
        // 403/404는 여기서 동기적으로 던져진다 — 스트림을 열기 전이라 GlobalExceptionHandler가 정상 처리한다.
        val events = importJobService.subscribeProgress(apiUser.userId, marketId, jobId)
        // 완료/실패/취소 이벤트를 내보낸 뒤에는 더 이상 구독을 이어가지 않는다(스펙: 종료 이벤트 후 스트림 종료).
        val eventsUntilTerminal =
            events.transformWhile { event ->
                emit(event)
                event !is ImportJobEvent.Completed && event !is ImportJobEvent.Failed && event !is ImportJobEvent.Canceled
            }

        val emitter = SseEmitter(SSE_TIMEOUT.toMillis())
        val collectJob =
            scope.launch(MDCContext()) {
                try {
                    eventsUntilTerminal.collect { event -> emitter.send(toSseEvent(event)) }
                    emitter.complete()
                } catch (e: Exception) {
                    logger.warn(e) { "SSE 스트림 중 오류: jobId=$jobId" }
                    emitter.completeWithError(e)
                }
            }
        emitter.onCompletion { collectJob.cancel() }
        emitter.onTimeout { emitter.complete() }
        emitter.onError { collectJob.cancel() }

        return emitter
    }

    @PostMapping("/{jobId}/cancel")
    override suspend fun cancelImport(
        apiUser: OwnerApiUser,
        @PathVariable marketId: Long,
        @PathVariable jobId: String,
    ): ApiResponse<Unit> {
        importJobService.cancel(apiUser.userId, marketId, jobId)

        return ApiResponse.success()
    }

    private fun toSseEvent(event: ImportJobEvent): SseEmitter.SseEventBuilder =
        when (event) {
            is ImportJobEvent.Progress -> SseEmitter.event().name("progress").data(ImportProgressResponse(event))
            is ImportJobEvent.Completed -> SseEmitter.event().name("completed").data(ImportCompletedResponse(event))
            is ImportJobEvent.Failed -> SseEmitter.event().name("failed").data(ImportFailedResponse(event))
            is ImportJobEvent.Canceled -> SseEmitter.event().name("canceled").data(ImportCanceledResponse(event))
        }

    @PreDestroy
    fun shutdown() {
        scope.cancel()
    }

    companion object {
        private val SSE_TIMEOUT = Duration.ofMinutes(30)
    }
}
