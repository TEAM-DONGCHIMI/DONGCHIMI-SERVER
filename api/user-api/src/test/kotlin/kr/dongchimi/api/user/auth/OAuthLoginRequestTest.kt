package kr.dongchimi.api.user.auth

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kr.dongchimi.api.user.auth.request.OAuthLoginRequest
import kr.dongchimi.core.auth.AuthErrorCode
import kr.dongchimi.core.auth.OAuthLoginCommand
import kr.dongchimi.core.common.exception.CommonErrorCode
import kr.dongchimi.core.common.exception.CoreException
import kr.dongchimi.core.user.SocialProvider

class OAuthLoginRequestTest :
    FunSpec({
        test("정상 입력이면 OAuthLoginCommand를 반환한다") {
            val command = OAuthLoginRequest("valid-token").toCommand("kakao")

            command shouldBe OAuthLoginCommand(SocialProvider.KAKAO, "valid-token")
        }

        test("provider 대소문자와 무관하게 매핑한다") {
            val command = OAuthLoginRequest("valid-token").toCommand("KaKaO")

            command shouldBe OAuthLoginCommand(SocialProvider.KAKAO, "valid-token")
        }

        test("액세스 토큰이 공백이면 예외가 발생한다") {
            val exception =
                shouldThrow<CoreException> {
                    OAuthLoginRequest(" ").toCommand("kakao")
                }

            exception.errorCode shouldBe CommonErrorCode.INVALID_INPUT
        }

        test("지원하지 않는 provider면 예외가 발생한다") {
            val exception =
                shouldThrow<CoreException> {
                    OAuthLoginRequest("valid-token").toCommand("naver")
                }

            exception.errorCode shouldBe AuthErrorCode.UNSUPPORTED_OAUTH_PROVIDER
        }
    })
