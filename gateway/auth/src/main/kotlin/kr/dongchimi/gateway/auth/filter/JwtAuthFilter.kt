package kr.dongchimi.gateway.auth.filter

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kr.dongchimi.gateway.auth.jwt.JwtProvider
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter

class JwtAuthFilter(
    private val jwtProvider: JwtProvider,
    private val headerTokenExtractor: HeaderTokenExtractor,
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val extractToken = headerTokenExtractor.extractToken(request)

        extractToken?.let { token ->
            runCatching { jwtProvider.parseAuthentication(token) }
                .onSuccess { authentication ->
                    SecurityContextHolder.getContext().authentication = authentication
                }
        }
        // NOTE: 추후 예외 처리 세팅 후 예외처리 구현
        filterChain.doFilter(request, response)
    }
}
