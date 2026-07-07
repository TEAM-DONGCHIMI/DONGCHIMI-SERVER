package kr.dongchimi.client.monitoring

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.core.spec.style.FunSpec
import kr.dongchimi.core.monitoring.ErrorContext

class SentryErrorNotifierTest :
    FunSpec({
        fun context() =
            ErrorContext(
                throwable = IllegalStateException("boom"),
                requestId = "rid-123",
                userId = "1",
                requestMethod = "POST",
                requestUri = "/v1/users",
                requestBody = null,
            )

        test("enabled=false면 Sentry를 호출하지 않고 조용히 반환한다") {
            val notifier = SentryErrorNotifier(SentryProperties(enabled = false))

            shouldNotThrowAny { notifier.notify(context()) }
        }

        test("enabled=true여도(미초기화 상태) 예외를 전파하지 않는다") {
            val notifier = SentryErrorNotifier(SentryProperties(enabled = true))

            shouldNotThrowAny { notifier.notify(context()) }
        }
    })
