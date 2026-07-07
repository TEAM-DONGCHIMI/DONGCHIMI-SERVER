package kr.dongchimi.api.owner.auth.request

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kr.dongchimi.core.common.exception.CommonErrorCode
import kr.dongchimi.core.common.exception.CoreException
import kr.dongchimi.core.owner.OwnerLoginCommand

class OwnerLoginRequestTest :
    FunSpec({
        test("정상 입력이면 OwnerLoginCommand를 반환한다") {
            val command = OwnerLoginRequest("owner@dongchimi.kr", "password123!", isAutoLogin = true).toCommand()

            command shouldBe OwnerLoginCommand("owner@dongchimi.kr", "password123!", isAutoLogin = true)
        }

        test("이메일 형식이 올바르지 않으면 INVALID_INPUT 예외가 발생한다") {
            val exception =
                shouldThrow<CoreException> {
                    OwnerLoginRequest("invalid-email", "password123!", isAutoLogin = true).toCommand()
                }

            exception.errorCode shouldBe CommonErrorCode.INVALID_INPUT
            exception.message shouldBe "올바른 이메일 형식이 아닙니다."
        }

        test("비밀번호가 비어 있으면 INVALID_INPUT 예외가 발생한다") {
            val exception =
                shouldThrow<CoreException> {
                    OwnerLoginRequest("owner@dongchimi.kr", " ", isAutoLogin = true).toCommand()
                }

            exception.errorCode shouldBe CommonErrorCode.INVALID_INPUT
            exception.message shouldBe "비밀번호를 입력해 주세요."
        }
    })
