package kr.dongchimi.gateway.logging

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kr.dongchimi.core.auth.PrincipalProvider
import org.slf4j.MDC
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.UUID

@Component
@Order(0)
class MdcFilter(
    private val principalProvider: PrincipalProvider,
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        try {
            MDC.put(REQUEST_ID, UUID.randomUUID().toString())

            getUserIdOrNull()?.let { MDC.put(USER_ID, it.toString()) }

            filterChain.doFilter(request, response)
        } finally {
            MDC.clear()
        }
    }

    private fun getUserIdOrNull(): Long? = runCatching { principalProvider.userId }.getOrNull()

    companion object {
        const val REQUEST_ID = "requestId"
        const val USER_ID = "userId"
    }
}
