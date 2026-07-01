package kr.dongchimi.gateway.logging

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kr.dongchimi.gateway.logging.config.LoggingProperties
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.util.AntPathMatcher
import org.springframework.web.filter.OncePerRequestFilter

private val logger = KotlinLogging.logger {}

@Component
@Order(1)
class LoggingFilter(
    private val loggingProperties: LoggingProperties,
) : OncePerRequestFilter() {

    private val pathMatcher = AntPathMatcher()

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val startTime = System.currentTimeMillis()
        try {
            filterChain.doFilter(request, response)
        } finally {
            val duration = System.currentTimeMillis() - startTime
            logger.info { "${request.method} ${request.requestURI} ${response.status} (${duration}ms)" }
        }
    }

    override fun shouldNotFilter(request: HttpServletRequest): Boolean =
        loggingProperties.excludePaths.any { pattern ->
            pathMatcher.match(pattern, request.requestURI)
        }
}