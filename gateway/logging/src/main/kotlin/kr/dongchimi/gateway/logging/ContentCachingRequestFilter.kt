package kr.dongchimi.gateway.logging

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.util.ContentCachingRequestWrapper

/**
 * 요청 본문을 다시 읽을 수 있도록 [ContentCachingRequestWrapper]로 감싼다.
 * 예외 발생 시 GlobalExceptionHandler가 캐시된 본문을 에러 알림에 담기 위함이다.
 * 하위 필터·컨트롤러가 모두 래퍼를 받도록 가장 바깥(@Order(-1))에서 감싼다.
 */
@Component
@Order(-1)
class ContentCachingRequestFilter : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val wrapped =
            if (request is ContentCachingRequestWrapper) {
                request
            } else {
                ContentCachingRequestWrapper(request, CACHE_LIMIT_BYTES)
            }
        filterChain.doFilter(wrapped, response)
    }

    companion object {
        // 요청당 캐시 상한(64KB). 이보다 큰 본문은 잘려 JSON 파싱이 실패하고 알림 본문은 null 처리된다.
        private const val CACHE_LIMIT_BYTES = 64 * 1024
    }
}
