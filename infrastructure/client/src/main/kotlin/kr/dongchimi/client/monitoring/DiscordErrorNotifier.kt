package kr.dongchimi.client.monitoring

import io.github.oshai.kotlinlogging.KotlinLogging
import kr.dongchimi.core.monitoring.ErrorContext
import kr.dongchimi.core.monitoring.ErrorNotifier
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import java.time.Instant

private val logger = KotlinLogging.logger {}

@Component
class DiscordErrorNotifier(
    private val discordProperties: DiscordProperties,
    @Qualifier("discordRestClient") private val restClient: RestClient,
) : ErrorNotifier {
    override fun notify(context: ErrorContext) {
        if (!discordProperties.enabled || discordProperties.webhookUrl.isBlank()) return

        restClient
            .post()
            .uri(discordProperties.webhookUrl)
            .contentType(MediaType.APPLICATION_JSON)
            .body(buildPayload(context))
            .retrieve()
            .toBodilessEntity()

        logger.debug { "Discord 에러 알림 전송 완료" }
    }

    private fun buildPayload(context: ErrorContext): DiscordWebhookPayload {
        val throwable = context.throwable

        val fields =
            buildList {
                add(DiscordField("requestId", context.requestId ?: "-", true))
                add(DiscordField("userId", context.userId ?: "-", true))
                add(
                    DiscordField(
                        name = "endpoint",
                        value = "${context.requestMethod ?: "-"} ${context.requestUri ?: "-"}",
                        inline = false,
                    ),
                )
                context.requestBody?.let {
                    add(DiscordField("requestBody", codeBlock(it, FIELD_LIMIT), false))
                }
            }

        val description =
            buildString {
                append("**${throwable::class.simpleName}**: ")
                append((throwable.message ?: "메시지 없음").take(MESSAGE_LIMIT))
                append("\n")
                append(codeBlock(throwable.stackTraceToString(), STACKTRACE_LIMIT))
            }

        return DiscordWebhookPayload(
            embeds =
                listOf(
                    DiscordEmbed(
                        title = "🚨 서버 오류 발생",
                        description = description.take(DESCRIPTION_LIMIT),
                        color = ERROR_COLOR,
                        fields = fields,
                        timestamp = Instant.now().toString(),
                    ),
                ),
        )
    }

    private fun codeBlock(
        text: String,
        max: Int,
    ): String = "```\n${text.take(max)}\n```"

    companion object {
        private const val ERROR_COLOR = 0xE74C3C
        private const val MESSAGE_LIMIT = 500
        private const val STACKTRACE_LIMIT = 1500
        private const val DESCRIPTION_LIMIT = 4000
        private const val FIELD_LIMIT = 1000
    }
}
