package kr.dongchimi.client.monitoring

import io.kotest.core.spec.style.FunSpec
import kr.dongchimi.core.monitoring.ErrorContext
import org.hamcrest.Matchers.containsString
import org.springframework.http.HttpMethod
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.content
import org.springframework.test.web.client.match.MockRestRequestMatchers.method
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import org.springframework.web.client.RestClient

class DiscordErrorNotifierTest :
    FunSpec({
        val webhookUrl = "https://discord.test/api/webhooks/1/token"

        fun context() =
            ErrorContext(
                throwable = IllegalStateException("boom"),
                requestId = "rid-123",
                userId = "1",
                requestMethod = "POST",
                requestUri = "/v1/users",
                requestBody = """{"name":"kim"}""",
            )

        test("enabled면 웹훅 URL로 임베드 페이로드를 전송한다") {
            val properties = DiscordProperties(enabled = true, webhookUrl = webhookUrl)
            val builder = RestClient.builder()
            val mockServer = MockRestServiceServer.bindTo(builder).build()
            val notifier = DiscordErrorNotifier(properties, builder.build())

            mockServer
                .expect(requestTo(webhookUrl))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().string(containsString("embeds")))
                .andExpect(content().string(containsString("boom")))
                .andExpect(content().string(containsString("rid-123")))
                .andRespond(withSuccess())

            notifier.notify(context())

            mockServer.verify()
        }

        test("enabled=false면 아무 요청도 보내지 않는다") {
            val properties = DiscordProperties(enabled = false, webhookUrl = webhookUrl)
            val builder = RestClient.builder()
            val mockServer = MockRestServiceServer.bindTo(builder).build()
            val notifier = DiscordErrorNotifier(properties, builder.build())

            notifier.notify(context())

            mockServer.verify() // 기대한 요청이 없고 실제 요청도 없어야 통과
        }

        test("webhookUrl이 비어 있으면 아무 요청도 보내지 않는다") {
            val properties = DiscordProperties(enabled = true, webhookUrl = "")
            val builder = RestClient.builder()
            val mockServer = MockRestServiceServer.bindTo(builder).build()
            val notifier = DiscordErrorNotifier(properties, builder.build())

            notifier.notify(context())

            mockServer.verify()
        }
    })
