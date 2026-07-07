package kr.dongchimi.api.core.swagger

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.maps.shouldContainKey
import io.kotest.matchers.maps.shouldNotContainKey
import io.kotest.matchers.shouldBe
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.responses.ApiResponses
import kr.dongchimi.api.core.common.dto.ApiResponse
import kr.dongchimi.api.core.common.swagger.ApiErrorCodes
import kr.dongchimi.api.core.common.swagger.ApiErrorCodesCustomizer
import kr.dongchimi.core.common.exception.ErrorCode
import org.springframework.web.method.HandlerMethod

private enum class TestErrorCode(
    override val status: Int,
    override val message: String,
) : ErrorCode {
    NOT_FOUND_A(404, "A를 찾을 수 없습니다."),
    NOT_FOUND_B(404, "B를 찾을 수 없습니다."),
    INVALID_INPUT(400, "유효하지 않은 입력값입니다."),
}

private class TestController {
    @ApiErrorCodes(TestErrorCode::class)
    fun withAnnotation() = Unit

    fun withoutAnnotation() = Unit
}

class ApiErrorCodesCustomizerTest :
    FunSpec({
        val customizer = ApiErrorCodesCustomizer()

        fun handlerMethodOf(methodName: String): HandlerMethod {
            val controller = TestController()
            val method = TestController::class.java.getDeclaredMethod(methodName)
            return HandlerMethod(controller, method)
        }

        test("어노테이션이 있으면 status별로 응답이 생성된다") {
            val operation = Operation().responses(ApiResponses())

            val result = customizer.customize(operation, handlerMethodOf("withAnnotation"))

            result.responses shouldContainKey "404"
            result.responses shouldContainKey "400"
        }

        test("같은 status의 코드가 여러 개면 example이 코드 개수만큼 병합된다") {
            val operation = Operation().responses(ApiResponses())

            val result = customizer.customize(operation, handlerMethodOf("withAnnotation"))

            val examples =
                result.responses["404"]
                    ?.content
                    ?.get("application/json")
                    ?.examples
            examples?.keys shouldBe setOf("NOT_FOUND_A", "NOT_FOUND_B")
        }

        test("example value의 code 필드가 enum name과 일치한다") {
            val operation = Operation().responses(ApiResponses())

            val result = customizer.customize(operation, handlerMethodOf("withAnnotation"))

            val example =
                result.responses["400"]
                    ?.content
                    ?.get("application/json")
                    ?.examples
                    ?.get("INVALID_INPUT")
            val value = example?.value as ApiResponse<*>
            value.code shouldBe "INVALID_INPUT"
        }

        test("어노테이션이 없으면 operation을 그대로 반환한다") {
            val operation = Operation().responses(ApiResponses())

            val result = customizer.customize(operation, handlerMethodOf("withoutAnnotation"))

            result.responses shouldNotContainKey "404"
            result.responses shouldNotContainKey "400"
        }
    })
