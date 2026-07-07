package kr.dongchimi.core.upload

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kr.dongchimi.core.common.exception.CoreException

private val TEST_PROPERTIES =
    UploadProperties(
        maxSizeBytes =
            mapOf(
                UploadPurpose.PRODUCT_THUMBNAIL to 5 * 1024 * 1024L,
                UploadPurpose.DEFAULT_PRODUCT_THUMBNAIL to 5 * 1024 * 1024L,
            ),
    )

class UploadValidatorTest :
    FunSpec({
        val validator = UploadValidator(TEST_PROPERTIES)

        test("허용된 content-type과 크기면 통과한다") {
            validator.validate(UploadPurpose.PRODUCT_THUMBNAIL, "image/jpeg", 1024L)
        }

        test("허용되지 않은 content-type이면 예외가 발생한다") {
            val exception =
                shouldThrow<CoreException> {
                    validator.validate(UploadPurpose.PRODUCT_THUMBNAIL, "application/pdf", 1024L)
                }

            exception.errorCode shouldBe UploadErrorCode.UNSUPPORTED_CONTENT_TYPE
        }

        test("최대 크기를 초과하면 예외가 발생한다") {
            val exception =
                shouldThrow<CoreException> {
                    validator.validate(UploadPurpose.PRODUCT_THUMBNAIL, "image/jpeg", 10 * 1024 * 1024L)
                }

            exception.errorCode shouldBe UploadErrorCode.FILE_TOO_LARGE
        }
    })
