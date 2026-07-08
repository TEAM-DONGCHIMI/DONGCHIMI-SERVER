package kr.dongchimi.api.core.exception

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kr.dongchimi.api.core.common.exception.ErrorNotificationDispatcher
import kr.dongchimi.api.core.common.exception.GlobalExceptionHandler
import kr.dongchimi.api.core.common.exception.RequestBodySanitizer
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

class GlobalExceptionHandlerTest :
    FunSpec({
        val sanitizer = RequestBodySanitizer(JsonMapper.builder().build())

        // Dispatchers.Unconfined는 launch 본문을 호출 스레드에서 동기 실행하므로 발송 결과를 즉시 검증할 수 있다.
        fun handlerWith(notifier: ErrorNotifier): GlobalExceptionHandler {
            val dispatcher = ErrorNotificationDispatcher(listOf(notifier), CoroutineScope(Dispatchers.Unconfined))
            return GlobalExceptionHandler(dispatcher, sanitizer)
        }

        test("500 응답을 반환하고 완성된 컨텍스트를 디스패처로 전달한다") {
            val recording = RecordingNotifier()
            val handler = handlerWith(recording)
            val request = MockHttpServletRequest("POST", "/v1/users")

            val response = handler.handleInternalException(RuntimeException("boom"), request)

            response.statusCode.value() shouldBe 500
            recording.received.shouldNotBeNull()
            recording.received!!.throwable.message shouldBe "boom"
            recording.received!!.requestMethod shouldBe "POST"
            recording.received!!.requestUri shouldBe "/v1/users"
        }

        test("CoreException 경로는 발송하지 않는다") {
            val recording = RecordingNotifier()
            val handler = handlerWith(recording)

            val response = handler.handleCoreException(CoreException(CommonErrorCode.INVALID_INPUT))

            response.statusCode.value() shouldBe CommonErrorCode.INVALID_INPUT.status
            recording.received.shouldBeNull()
        }
    })
