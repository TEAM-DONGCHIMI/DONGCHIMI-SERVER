package kr.dongchimi.gateway.auth.security

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kr.dongchimi.core.common.exception.CommonErrorCode
import kr.dongchimi.core.common.exception.CoreException
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerExceptionResolver

private val logger = KotlinLogging.logger {}

@Component
class DelegatedAccessDeniedHandler(
    @Qualifier("handlerExceptionResolver") private val handlerExceptionResolver: HandlerExceptionResolver,
) : AccessDeniedHandler {
    override fun handle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        accessDeniedException: AccessDeniedException,
    ) {
        logger.warn(accessDeniedException) { "접근 거부: ${request.method} ${request.requestURI} - ${accessDeniedException.message}" }
        handlerExceptionResolver.resolveException(request, response, null, CoreException(CommonErrorCode.FORBIDDEN))
    }
}
