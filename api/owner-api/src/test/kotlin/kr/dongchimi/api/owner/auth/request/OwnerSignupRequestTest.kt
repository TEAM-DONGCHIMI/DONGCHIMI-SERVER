package kr.dongchimi.api.owner.auth.request

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kr.dongchimi.core.common.exception.CommonErrorCode
import kr.dongchimi.core.common.exception.CoreException
import kr.dongchimi.core.owner.OwnerSignupCommand

class OwnerSignupRequestTest :
    FunSpec({
        test("정상 입력이면 OwnerSignupCommand를 반환한다") {
            val command = OwnerSignupRequest("owner@dongchimi.kr", "password123!").toCommand()

            command shouldBe OwnerSignupCommand("owner@dongchimi.kr", "password123!")
        }

        test("이메일 형식이 올바르지 않으면 INVALID_INPUT 예외가 발생한다") {
            val exception =
                shouldThrow<CoreException> {
                    OwnerSignupRequest("invalid-email", "password123!").toCommand()
                }

            exception.errorCode shouldBe CommonErrorCode.INVALID_INPUT
            exception.message shouldBe "올바르지 않은 이메일 형식입니다."
        }

        test("비밀번호가 6자 미만이면 INVALID_INPUT 예외가 발생한다") {
            val exception =
                shouldThrow<CoreException> {
                    OwnerSignupRequest("owner@dongchimi.kr", "12345").toCommand()
                }

            exception.errorCode shouldBe CommonErrorCode.INVALID_INPUT
            exception.message shouldBe "비밀번호는 6~20자로 입력해주세요."
        }

        test("비밀번호가 20자를 초과하면 INVALID_INPUT 예외가 발생한다") {
            val exception =
                shouldThrow<CoreException> {
                    OwnerSignupRequest("owner@dongchimi.kr", "a".repeat(21)).toCommand()
                }

            exception.errorCode shouldBe CommonErrorCode.INVALID_INPUT
            exception.message shouldBe "비밀번호는 6~20자로 입력해주세요."
        }

        test("비밀번호에 공백이 포함되면 INVALID_INPUT 예외가 발생한다") {
            val exception =
                shouldThrow<CoreException> {
                    OwnerSignupRequest("owner@dongchimi.kr", "pass word").toCommand()
                }

            exception.errorCode shouldBe CommonErrorCode.INVALID_INPUT
            exception.message shouldBe "비밀번호에 공백을 포함할 수 없습니다."
        }

        test("비밀번호에 한글이 포함되면 INVALID_INPUT 예외가 발생한다") {
            val exception =
                shouldThrow<CoreException> {
                    OwnerSignupRequest("owner@dongchimi.kr", "비밀번호1234").toCommand()
                }

            exception.errorCode shouldBe CommonErrorCode.INVALID_INPUT
            exception.message shouldBe "비밀번호에 한글을 포함할 수 없습니다."
        }
    })
