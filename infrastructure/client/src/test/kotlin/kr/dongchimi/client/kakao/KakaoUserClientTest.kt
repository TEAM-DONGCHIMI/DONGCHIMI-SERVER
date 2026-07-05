package kr.dongchimi.client.kakao

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kr.dongchimi.core.auth.AuthErrorCode
import kr.dongchimi.core.common.exception.CoreException
import kr.dongchimi.core.user.Gender
import kr.dongchimi.core.user.SocialProvider
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.header
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withStatus
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import org.springframework.web.client.RestClient

class KakaoUserClientTest :
    FunSpec({
        val properties = KakaoProperties(userInfoUri = "https://kapi.kakao.com/v2/user/me")

        test("카카오 사용자 정보를 SocialUserInfo로 매핑한다") {
            val builder = RestClient.builder()
            val mockServer = MockRestServiceServer.bindTo(builder).build()
            val client = KakaoUserClient(properties, builder)

            mockServer
                .expect(requestTo(properties.userInfoUri))
                .andExpect(header("Authorization", "Bearer kakao-token"))
                .andRespond(
                    withSuccess(
                        """
                        {
                            "id": 12345,
                            "kakao_account": {
                                "email": "a@dongchimi.kr",
                                "profile": { "nickname": "동치미" },
                                "gender": "female",
                                "age_range": "20~29"
                            }
                        }
                        """.trimIndent(),
                        MediaType.APPLICATION_JSON,
                    ),
                )

            val info = client.fetchUserInfo("kakao-token")

            info.account.provider shouldBe SocialProvider.KAKAO
            info.account.socialId shouldBe "12345"
            info.email shouldBe "a@dongchimi.kr"
            info.name shouldBe "동치미"
            info.gender shouldBe Gender.F
            info.age.shouldBeNull()
        }

        test("카카오 토큰이 유효하지 않으면 인증 실패 예외를 던진다") {
            val builder = RestClient.builder()
            val mockServer = MockRestServiceServer.bindTo(builder).build()
            val client = KakaoUserClient(properties, builder)

            mockServer
                .expect(requestTo(properties.userInfoUri))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED))

            val exception =
                shouldThrow<CoreException> {
                    client.fetchUserInfo("invalid-token")
                }

            exception.errorCode shouldBe AuthErrorCode.OAUTH_AUTHENTICATION_FAILED
        }
    })
