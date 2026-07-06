package kr.dongchimi.core.upload

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kr.dongchimi.core.common.exception.CoreException

class UploadContentTypeValidatorTest :
    FunSpec({
        val validator = UploadContentTypeValidator()

        test("허용된 content-type이면 통과한다") {
            validator.validate(UploadPurpose.PRODUCT_THUMBNAIL, "image/jpeg")
        }

        test("허용되지 않은 content-type이면 예외가 발생한다") {
            val exception =
                shouldThrow<CoreException> {
                    validator.validate(UploadPurpose.PRODUCT_THUMBNAIL, "application/pdf")
                }

            exception.errorCode shouldBe UploadErrorCode.UNSUPPORTED_CONTENT_TYPE
        }
    })
