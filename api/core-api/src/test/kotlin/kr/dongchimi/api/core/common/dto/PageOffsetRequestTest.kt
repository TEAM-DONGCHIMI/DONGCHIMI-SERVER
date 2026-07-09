package kr.dongchimi.api.core.common.dto

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kr.dongchimi.core.common.exception.CommonErrorCode
import kr.dongchimi.core.common.exception.CoreException

class PageOffsetRequestTest :
    FunSpec({
        test("page/size 미지정이면 기본값(0, 10)이 적용된다") {
            val request = PageOffsetRequest()

            val pageOffset = request.toPageOffset()

            pageOffset.page shouldBe 0
            pageOffset.size shouldBe 10
        }

        test("page가 음수면 INVALID_INPUT") {
            val request = PageOffsetRequest(page = -1)

            val exception = shouldThrow<CoreException> { request.toPageOffset() }

            exception.errorCode shouldBe CommonErrorCode.INVALID_INPUT
        }

        test("size가 범위를 벗어나면 INVALID_INPUT") {
            val request = PageOffsetRequest(size = 101)

            val exception = shouldThrow<CoreException> { request.toPageOffset() }

            exception.errorCode shouldBe CommonErrorCode.INVALID_INPUT
        }

        test("정상 범위면 PageOffset으로 변환된다") {
            val request = PageOffsetRequest(page = 2, size = 20)

            val pageOffset = request.toPageOffset()

            pageOffset.page shouldBe 2
            pageOffset.size shouldBe 20
        }
    })
