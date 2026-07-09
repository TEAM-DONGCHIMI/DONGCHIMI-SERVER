package kr.dongchimi.gateway.auth.filter

import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Component

@Component
class HeaderTokenExtractor {
    fun extractToken(request: HttpServletRequest): String? {
        val header = request.getHeader(AUTHORIZATION_HEADER) ?: return null
        if (!header.startsWith(BEARER_PREFIX)) return null

        return header.substring(BEARER_PREFIX.length)
    }

    companion object {
        private const val AUTHORIZATION_HEADER = "Authorization"
        private const val BEARER_PREFIX = "Bearer "
    }
}
