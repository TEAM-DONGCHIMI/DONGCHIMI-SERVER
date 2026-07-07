package kr.dongchimi.api.core.common.exception

import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Component
import org.springframework.web.util.ContentCachingRequestWrapper
import org.springframework.web.util.WebUtils
import tools.jackson.databind.ObjectMapper

/**
 * 에러 알림에 담을 요청 본문을 안전하게 추출한다.
 * JSON 요청에 한해 캡처하고, 지정 키를 마스킹한 뒤 [MAX_BODY_LENGTH]자로 truncate한다.
 * 비-JSON·본문 없음·파싱 실패 시 `null`을 반환한다(마스킹 보장 불가 시 아예 내보내지 않음).
 */
@Component
class RequestBodySanitizer(
    private val objectMapper: ObjectMapper,
) {
    fun sanitize(request: HttpServletRequest): String? {
        if (request.contentType?.contains("application/json", ignoreCase = true) != true) return null

        val wrapper =
            WebUtils.getNativeRequest(request, ContentCachingRequestWrapper::class.java) ?: return null
        val bytes = wrapper.contentAsByteArray
        if (bytes.isEmpty()) return null

        return runCatching {
            val parsed = objectMapper.readValue(bytes, Any::class.java)
            objectMapper.writeValueAsString(mask(parsed)).take(MAX_BODY_LENGTH)
        }.getOrNull()
    }

    private fun mask(value: Any?): Any? =
        when (value) {
            is Map<*, *> ->
                value.entries.associate { (key, child) ->
                    key to if (key is String && key.lowercase() in SENSITIVE_KEYS) MASKED else mask(child)
                }

            is List<*> -> value.map { mask(it) }
            else -> value
        }

    companion object {
        const val MAX_BODY_LENGTH = 2000
        private const val MASKED = "***"
        private val SENSITIVE_KEYS =
            setOf(
                "password",
                "passwd",
                "token",
                "accesstoken",
                "refreshtoken",
                "secret",
                "authorization",
                "pin",
                "cardnumber",
            )
    }
}
