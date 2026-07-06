package kr.dongchimi.client.kakao

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.restclient.autoconfigure.RestClientAutoConfiguration
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.web.client.RestClient
import java.util.function.Supplier

class KakaoRestClientConfigTest :
    FunSpec({
        test("RestClientAutoConfiguration이 주입하는 RestClient.Builder로 kakaoRestClient 빈이 정상 생성된다") {
            ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(RestClientAutoConfiguration::class.java))
                .withBean(
                    KakaoProperties::class.java,
                    Supplier { KakaoProperties(userInfoUri = "https://kapi.kakao.com/v2/user/me") },
                ).withUserConfiguration(KakaoRestClientConfig::class.java)
                .run { context ->
                    context.startupFailure.shouldBeNull()
                    context.getBean(RestClient::class.java)
                }
        }
    })
