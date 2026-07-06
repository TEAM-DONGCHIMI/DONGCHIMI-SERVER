package kr.dongchimi.client.monitoring

import io.github.oshai.kotlinlogging.KotlinLogging
import io.sentry.Sentry
import org.springframework.beans.factory.InitializingBean
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

/**
 * 애플리케이션 기동 시 Sentry SDK를 1회 초기화한다.
 * `enabled=true`이고 dsn이 설정된 경우에만 초기화하며, 그 외에는 no-op이다.
 */
@Component
class SentryInitializer(
    private val sentryProperties: SentryProperties,
) : InitializingBean {
    override fun afterPropertiesSet() {
        if (!sentryProperties.enabled || sentryProperties.dsn.isBlank()) return

        Sentry.init { options ->
            options.dsn = sentryProperties.dsn
            options.environment = sentryProperties.environment
            options.tracesSampleRate = sentryProperties.tracesSampleRate
        }
        logger.info { "Sentry 초기화 완료 (environment=${sentryProperties.environment})" }
    }
}
