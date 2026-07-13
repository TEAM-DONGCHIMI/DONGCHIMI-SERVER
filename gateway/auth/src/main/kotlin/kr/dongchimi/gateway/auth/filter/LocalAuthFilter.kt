package kr.dongchimi.gateway.auth.filter

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kr.dongchimi.gateway.auth.security.UserAuthentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter

class LocalAuthFilter(
    private val headerTokenExtractor: HeaderTokenExtractor,
) : OncePerRequestFilter() {
    /** SSE 등 비동기 응답이 완료될 때의 ASYNC dispatch에서도 SecurityContext를 다시 채워야 AuthorizationFilter가 통과한다. */
    override fun shouldNotFilterAsyncDispatch(): Boolean = false

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val token = headerTokenExtractor.extractToken(request)

        token?.let {
            if (it.contains("test")) {
                val split = it.split(":")
                val roleName = split[1].uppercase()

                if (split[0] == "test") {
                    SecurityContextHolder.getContext().authentication = UserAuthentication(1L, setOf(roleName))
                }
            }
        }

        // NOTE: 추후 예외 처리 세팅 후 예외처리 구현
        filterChain.doFilter(request, response)
    }
}
