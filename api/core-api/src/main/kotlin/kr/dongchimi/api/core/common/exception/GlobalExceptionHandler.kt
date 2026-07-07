package kr.dongchimi.api.core.common.exception

import io.github.oshai.kotlinlogging.KotlinLogging
import kr.dongchimi.api.core.common.dto.ApiResponse
import kr.dongchimi.core.common.exception.CommonErrorCode
import kr.dongchimi.core.common.exception.CoreException
import kr.dongchimi.gateway.logging.MdcFilter.Companion.REQUEST_ID
import kr.dongchimi.gateway.logging.MdcFilter.Companion.USER_ID
import org.slf4j.MDC
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

private val logger = KotlinLogging.logger {}

@RestControllerAdvice
class GlobalExceptionHandler {
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
    fun handleInternalException(exception: Exception): ResponseEntity<ApiResponse<Any>> {
        logger.error(exception) {
            "[requestId=${MDC.get(REQUEST_ID)}, userId=${MDC.get(USER_ID)}] ${exception.message}"
        }

        // 외부 에러 알림 (Discord, Sentry 연동)

        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error(CommonErrorCode.INTERNAL_SERVER_ERROR))
    }
}
