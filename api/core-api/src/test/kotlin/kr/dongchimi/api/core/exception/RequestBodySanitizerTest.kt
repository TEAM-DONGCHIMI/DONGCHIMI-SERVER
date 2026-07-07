package kr.dongchimi.api.core.exception

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.util.ContentCachingRequestWrapper
import tools.jackson.databind.json.JsonMapper

class RequestBodySanitizerTest :
    FunSpec({
        val sanitizer = RequestBodySanitizer(JsonMapper.builder().build())

        fun jsonRequest(body: String): ContentCachingRequestWrapper {
            val mock = MockHttpServletRequest("POST", "/v1/users")
            mock.contentType = "application/json"
            mock.setContent(body.toByteArray())
            val wrapper = ContentCachingRequestWrapper(mock, 64 * 1024)
            wrapper.inputStream.readBytes() // 읽어서 캐시를 채운다
            return wrapper
        }

        test("최상위 지정 키 값을 마스킹한다") {
            val result = sanitizer.sanitize(jsonRequest("""{"name":"kim","password":"secret123"}"""))

            result!!
            result shouldContain "\"name\":\"kim\""
            result shouldContain "***"
            result shouldNotContain "secret123"
        }

        test("중첩 객체와 배열 내부의 지정 키도 마스킹한다") {
            val body = """{"user":{"token":"abc"},"items":[{"pin":"1234"}]}"""

            val result = sanitizer.sanitize(jsonRequest(body))

            result!!
            result shouldNotContain "abc"
            result shouldNotContain "1234"
        }

        test("2000자를 초과하면 truncate한다") {
            val body = """{"note":"${"a".repeat(3000)}"}"""

            val result = sanitizer.sanitize(jsonRequest(body))

            result!!.length shouldBe RequestBodySanitizer.MAX_BODY_LENGTH
        }

        test("JSON이 아니면 null을 반환한다") {
            val mock = MockHttpServletRequest("POST", "/v1/users")
            mock.contentType = "text/plain"
            mock.setContent("hello".toByteArray())
            val wrapper = ContentCachingRequestWrapper(mock, 64 * 1024)
            wrapper.inputStream.readBytes()

            sanitizer.sanitize(wrapper).shouldBeNull()
        }

        test("본문이 비어 있으면 null을 반환한다") {
            sanitizer.sanitize(jsonRequest("")).shouldBeNull()
        }

        test("ContentCachingRequestWrapper가 아니면 null을 반환한다") {
            val mock = MockHttpServletRequest("POST", "/v1/users")
            mock.contentType = "application/json"
            mock.setContent("""{"name":"kim"}""".toByteArray())

            sanitizer.sanitize(mock).shouldBeNull()
        }

        test("JSON 파싱에 실패하면 null을 반환한다(마스킹 보장 불가)") {
            sanitizer.sanitize(jsonRequest("""{"broken": """)).shouldBeNull()
        }
    })
