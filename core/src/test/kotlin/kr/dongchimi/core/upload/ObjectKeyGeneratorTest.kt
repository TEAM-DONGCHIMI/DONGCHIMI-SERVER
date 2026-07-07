package kr.dongchimi.core.upload

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldMatch
import kr.dongchimi.core.common.exception.CoreException

class ObjectKeyGeneratorTest :
    FunSpec({
        val generator = ObjectKeyGenerator()

        test("tmp 경로 형식의 임시 key를 생성한다") {
            val tempKey = generator.generateTempKey(UploadPurpose.PRODUCT_THUMBNAIL, "image/jpeg")

            tempKey shouldMatch Regex("^tmp/PRODUCT_THUMBNAIL/[0-9a-f-]+\\.jpg$")
        }

        test("임시 key에서 purpose를 파싱한다") {
            val tempKey = generator.generateTempKey(UploadPurpose.DEFAULT_PRODUCT_THUMBNAIL, "image/png")

            generator.parsePurpose(tempKey) shouldBe UploadPurpose.DEFAULT_PRODUCT_THUMBNAIL
        }

        test("tmp 접두사가 아니면 예외가 발생한다") {
            val exception =
                shouldThrow<CoreException> {
                    generator.parsePurpose("products/thumbnails/2026/07/abc.jpg")
                }

            exception.errorCode shouldBe UploadErrorCode.INVALID_UPLOAD_KEY
        }

        test("알 수 없는 purpose면 예외가 발생한다") {
            val exception =
                shouldThrow<CoreException> {
                    generator.parsePurpose("tmp/UNKNOWN_PURPOSE/abc.jpg")
                }

            exception.errorCode shouldBe UploadErrorCode.INVALID_UPLOAD_KEY
        }

        test("정식 key로 변환하면 purpose의 prefix와 연월 경로를 갖는다") {
            val tempKey = generator.generateTempKey(UploadPurpose.PRODUCT_THUMBNAIL, "image/webp")

            val permanentKey = generator.toPermanentKey(tempKey)

            val fileName = tempKey.substringAfterLast("/")
            permanentKey shouldMatch Regex("^products/thumbnails/\\d{4}/\\d{2}/$fileName$")
        }
    })
