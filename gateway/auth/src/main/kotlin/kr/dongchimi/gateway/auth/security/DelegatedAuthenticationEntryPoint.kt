package kr.dongchimi.gateway.auth.security

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kr.dongchimi.core.common.exception.CommonErrorCode
import kr.dongchimi.core.common.exception.CoreException
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerExceptionResolver

private val logger = KotlinLogging.logger {}

@Component
class DelegatedAuthenticationEntryPoint(
    @Qualifier("handlerExceptionResolver") private val handlerExceptionResolver: HandlerExceptionResolver,
) : AuthenticationEntryPoint {
    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException,
    ) {
        logger.warn(authException) { "인증 실패: ${request.method} ${request.requestURI} - ${authException.message}" }
        handlerExceptionResolver.resolveException(request, response, null, CoreException(CommonErrorCode.UNAUTHORIZED))
    }
}
