package kr.dongchimi.gateway.logging

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

private val logger = KotlinLogging.logger {}

@Component
@Order(1)
class LoggingFilter : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val startTime = System.currentTimeMillis()
        filterChain.doFilter(request, response)
        val duration = System.currentTimeMillis() - startTime

        logger.info { "${request.method} ${request.requestURI} ${response.status} (${duration}ms)" }
    }

    override fun shouldNotFilter(request: HttpServletRequest): Boolean =
        request.requestURI.startsWith("/actuator")
}
