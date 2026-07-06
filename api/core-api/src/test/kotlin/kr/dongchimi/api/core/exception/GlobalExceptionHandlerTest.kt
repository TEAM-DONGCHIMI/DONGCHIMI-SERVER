package kr.dongchimi.api.core.exception

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kr.dongchimi.core.common.exception.CommonErrorCode
import kr.dongchimi.core.common.exception.CoreException
import kr.dongchimi.core.monitoring.ErrorContext
import kr.dongchimi.core.monitoring.ErrorNotifier
import org.springframework.mock.web.MockHttpServletRequest
import tools.jackson.databind.json.JsonMapper

private class RecordingNotifier : ErrorNotifier {
    var received: ErrorContext? = null

    override fun notify(context: ErrorContext) {
        received = context
    }
}

private class ThrowingNotifier : ErrorNotifier {
    override fun notify(context: ErrorContext): Unit = throw RuntimeException("notifier down")
}

class GlobalExceptionHandlerTest :
    FunSpec({
        val sanitizer = RequestBodySanitizer(JsonMapper.builder().build())

        test("500 응답을 반환하고 모든 notifier에 컨텍스트를 전달한다") {
            val recording = RecordingNotifier()
            val handler = GlobalExceptionHandler(listOf(recording), sanitizer)
            val request = MockHttpServletRequest("POST", "/v1/users")

            val response = handler.handleInternalException(RuntimeException("boom"), request)

            response.statusCode.value() shouldBe 500
            recording.received.shouldNotBeNull()
            recording.received!!.throwable.message shouldBe "boom"
            recording.received!!.requestMethod shouldBe "POST"
            recording.received!!.requestUri shouldBe "/v1/users"
        }

        test("한 notifier가 예외를 던져도 응답과 다른 notifier에 영향이 없다") {
            val recording = RecordingNotifier()
            val handler = GlobalExceptionHandler(listOf(ThrowingNotifier(), recording), sanitizer)

            val response = handler.handleInternalException(RuntimeException("boom"), MockHttpServletRequest())

            response.statusCode.value() shouldBe 500
            recording.received.shouldNotBeNull()
        }

        test("CoreException 경로는 notify를 호출하지 않는다") {
            val recording = RecordingNotifier()
            val handler = GlobalExceptionHandler(listOf(recording), sanitizer)

            val response = handler.handleCoreException(CoreException(CommonErrorCode.INVALID_INPUT))

            response.statusCode.value() shouldBe CommonErrorCode.INVALID_INPUT.status
            recording.received.shouldBeNull()
        }
    })
