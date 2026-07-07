package kr.dongchimi.api.core.exception

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kr.dongchimi.core.monitoring.ErrorContext
import kr.dongchimi.core.monitoring.ErrorNotifier

private class CapturingNotifier : ErrorNotifier {
    var received: ErrorContext? = null

    override fun notify(context: ErrorContext) {
        received = context
    }
}

private class ThrowingNotifier : ErrorNotifier {
    override fun notify(context: ErrorContext): Unit = throw RuntimeException("notifier down")
}

class ErrorNotificationDispatcherTest :
    FunSpec({
        val context =
            ErrorContext(
                throwable = RuntimeException("boom"),
                requestId = "req-1",
                userId = "1",
                requestMethod = "POST",
                requestUri = "/v1/users",
                requestBody = null,
            )

        // Dispatchers.Unconfined는 launch 본문을 호출 스레드에서 동기 실행하므로 결과를 즉시 검증할 수 있다.
        fun dispatcherWith(vararg notifiers: ErrorNotifier) =
            ErrorNotificationDispatcher(notifiers.toList(), CoroutineScope(Dispatchers.Unconfined))

        test("모든 notifier에 컨텍스트를 전달한다") {
            val first = CapturingNotifier()
            val second = CapturingNotifier()
            val dispatcher = dispatcherWith(first, second)

            dispatcher.dispatch(context)

            first.received.shouldNotBeNull()
            second.received.shouldNotBeNull()
            first.received!!.throwable.message shouldBe "boom"
        }

        test("한 notifier가 예외를 던져도 다른 notifier에 영향이 없다") {
            val recording = CapturingNotifier()
            val dispatcher = dispatcherWith(ThrowingNotifier(), recording)

            dispatcher.dispatch(context)

            recording.received.shouldNotBeNull()
        }
    })
