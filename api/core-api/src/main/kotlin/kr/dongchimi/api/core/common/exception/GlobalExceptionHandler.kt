package kr.dongchimi.api.core.common.exception

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest
import kr.dongchimi.api.core.common.dto.ApiResponse
import kr.dongchimi.core.common.exception.CommonErrorCode
import kr.dongchimi.core.common.exception.CoreException
import kr.dongchimi.core.common.exception.ErrorCode
import kr.dongchimi.core.monitoring.ErrorContext
import kr.dongchimi.core.monitoring.ErrorNotificationDispatcher
import kr.dongchimi.gateway.logging.MdcFilter.Companion.REQUEST_ID
import kr.dongchimi.gateway.logging.MdcFilter.Companion.USER_ID
import org.slf4j.MDC
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.HttpMediaTypeNotSupportedException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MissingRequestHeaderException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.servlet.resource.NoResourceFoundException

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

    @ExceptionHandler(NoResourceFoundException::class)
    fun handleNoResourceFound(exception: NoResourceFoundException) = handleSpringWebException(CommonErrorCode.RESOURCE_NOT_FOUND, exception)

    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun handleMethodNotSupported(exception: HttpRequestMethodNotSupportedException) =
        handleSpringWebException(CommonErrorCode.METHOD_NOT_ALLOWED, exception)

    @ExceptionHandler(HttpMediaTypeNotSupportedException::class)
    fun handleMediaTypeNotSupported(exception: HttpMediaTypeNotSupportedException) =
        handleSpringWebException(CommonErrorCode.UNSUPPORTED_MEDIA_TYPE, exception)

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleMessageNotReadable(exception: HttpMessageNotReadableException) =
        handleSpringWebException(CommonErrorCode.INVALID_REQUEST_BODY, exception)

    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun handleMissingParameter(exception: MissingServletRequestParameterException) =
        handleSpringWebException(CommonErrorCode.MISSING_REQUEST_PARAMETER, exception)

    @ExceptionHandler(MissingRequestHeaderException::class)
    fun handleMissingHeader(exception: MissingRequestHeaderException) =
        handleSpringWebException(CommonErrorCode.MISSING_REQUEST_HEADER, exception)

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleTypeMismatch(exception: MethodArgumentTypeMismatchException) =
        handleSpringWebException(CommonErrorCode.TYPE_MISMATCH, exception)

    private fun handleSpringWebException(
        errorCode: ErrorCode,
        exception: Exception,
    ): ResponseEntity<ApiResponse<Any>> {
        logger.warn {
            "[requestId=${MDC.get(REQUEST_ID)}, userId=${MDC.get(USER_ID)}] (${errorCode.name}) ${exception.message}"
        }

        return ResponseEntity
            .status(errorCode.status)
            .body(ApiResponse.error(errorCode))
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
