package kr.dongchimi.client.monitoring

import io.sentry.Sentry
import kr.dongchimi.core.monitoring.ErrorContext
import kr.dongchimi.core.monitoring.ErrorNotifier
import org.springframework.stereotype.Component

@Component
class SentryErrorNotifier(
    private val sentryProperties: SentryProperties,
) : ErrorNotifier {
    override fun notify(context: ErrorContext) {
        if (!sentryProperties.enabled) return

        Sentry.withScope { scope ->
            context.requestId?.let { scope.setTag("requestId", it) }
            context.userId?.let { scope.setTag("userId", it) }
            context.requestMethod?.let { scope.setTag("method", it) }
            context.requestUri?.let { scope.setTag("uri", it) }
            context.requestBody?.let { scope.setExtra("requestBody", it) }
            Sentry.captureException(context.throwable)
        }
    }
}
