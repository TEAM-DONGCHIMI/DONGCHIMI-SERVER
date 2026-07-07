package kr.dongchimi.api.core.common.exception

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PreDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.slf4j.MDCContext
import kr.dongchimi.core.monitoring.ErrorContext
import kr.dongchimi.core.monitoring.ErrorNotifier
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

/**
 * 미처리 예외 알림을 요청 스레드에서 분리해 비동기(fire-and-forget)로 발송한다.
 *
 * [ErrorContext]는 반드시 요청 스레드에서 완성해 넘겨야 한다. MDC·HttpServletRequest는
 * ThreadLocal/요청 스코프라 코루틴 스레드에서 접근하면 값이 유실되기 때문이다.
 */
@Component
class ErrorNotificationDispatcher(
    private val errorNotifiers: List<ErrorNotifier>,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
) {
    fun dispatch(context: ErrorContext) {
        // dispatch()는 요청 스레드에서 호출됨 → 그 시점 MDC를 MDCContext로 캡처해 코루틴에 전파한다.
        scope.launch(MDCContext()) {
            errorNotifiers.forEach { notifier ->
                runCatching { notifier.notify(context) }
                    .onFailure { logger.warn(it) { "에러 알림 실패: ${notifier::class.simpleName}" } }
            }
        }
    }

    @PreDestroy
    fun shutdown() {
        scope.cancel()
    }
}
