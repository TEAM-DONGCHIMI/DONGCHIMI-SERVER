package kr.dongchimi.api.core.common.exception

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest
import kr.dongchimi.api.core.common.dto.ApiResponse
import kr.dongchimi.core.common.exception.CommonErrorCode
import kr.dongchimi.core.common.exception.CoreException
import kr.dongchimi.core.monitoring.ErrorContext
import kr.dongchimi.gateway.logging.MdcFilter.Companion.REQUEST_ID
import kr.dongchimi.gateway.logging.MdcFilter.Companion.USER_ID
import org.slf4j.MDC
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

private val logger = KotlinLogging.logger {}

@RestControllerAdvice
class GlobalExceptionHandler(
    private val errorNotificationDispatcher: ErrorNotificationDispatcher,
    private val requestBodySanitizer: RequestBodySanitizer,
) {
    @ExceptionHandler(CoreException::class)
    fun handleCoreException(exception: CoreException): ResponseEntity<ApiResponse<Any>> {
        logger.warn {
            "[requestId=${MDC.get(REQUEST_ID)}, userId=${MDC.get(USER_ID)}] (${exception.errorCode.name}) ${exception.message}"
        }

        return ResponseEntity
            .status(exception.errorCode.status)
            .body(ApiResponse.error(exception))
    }

    @ExceptionHandler(Exception::class)
    fun handleInternalException(
        exception: Exception,
        request: HttpServletRequest,
    ): ResponseEntity<ApiResponse<Any>> {
        logger.error(exception) {
            "[requestId=${MDC.get(REQUEST_ID)}, userId=${MDC.get(USER_ID)}] ${exception.message}"
        }

        notifyAll(exception, request)

        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error(CommonErrorCode.INTERNAL_SERVER_ERROR))
    }

    private fun notifyAll(
        exception: Exception,
        request: HttpServletRequest,
    ) {
        val context =
            ErrorContext(
                throwable = exception,
                requestId = MDC.get(REQUEST_ID),
                userId = MDC.get(USER_ID),
                requestMethod = request.method,
                requestUri = request.requestURI,
                requestBody = runCatching { requestBodySanitizer.sanitize(request) }.getOrNull(),
            )

        // 컨텍스트는 요청 스레드에서 완성했으므로, 실제 발송은 디스패처에 위임해 비동기로 처리한다.
        errorNotificationDispatcher.dispatch(context)
    }
}
