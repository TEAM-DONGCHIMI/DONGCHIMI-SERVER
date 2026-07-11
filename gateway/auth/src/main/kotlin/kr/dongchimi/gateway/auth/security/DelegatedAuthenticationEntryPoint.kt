package kr.dongchimi.gateway.auth.security

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kr.dongchimi.core.common.exception.CommonErrorCode
import kr.dongchimi.core.common.exception.CoreException
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerExceptionResolver

@Component
class DelegatedAuthenticationEntryPoint(
    @Qualifier("handlerExceptionResolver") private val handlerExceptionResolver: HandlerExceptionResolver,
) : AuthenticationEntryPoint {
    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException,
    ) {
        handlerExceptionResolver.resolveException(request, response, null, CoreException(CommonErrorCode.UNAUTHORIZED))
    }
}
