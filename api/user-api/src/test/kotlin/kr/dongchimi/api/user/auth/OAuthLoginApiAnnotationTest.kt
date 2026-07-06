package kr.dongchimi.api.user.auth

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import kr.dongchimi.api.core.swagger.ApiErrorCodes
import kr.dongchimi.core.auth.OAuthLoginService
import org.mockito.Mockito
import org.springframework.core.annotation.AnnotatedElementUtils
import org.springframework.web.method.HandlerMethod

class OAuthLoginApiAnnotationTest :
    FunSpec({
        val controller = OAuthLoginController(Mockito.mock(OAuthLoginService::class.java))
        val loginMethod = OAuthLoginController::class.java.getMethod("login", String::class.java, OAuthLoginRequest::class.java)
        val handlerMethod = HandlerMethod(controller, loginMethod)

        test("인터페이스에 선언한 클래스 레벨 @Tag를 컨트롤러 구현체에서 찾을 수 있다") {
            AnnotatedElementUtils.findMergedAnnotation(OAuthLoginController::class.java, Tag::class.java).shouldNotBeNull()
        }

        test("인터페이스에 선언한 메서드 레벨 @Operation을 override 메서드에서 찾을 수 있다") {
            handlerMethod.getMethodAnnotation(Operation::class.java).shouldNotBeNull()
        }

        test("인터페이스에 선언한 메서드 레벨 @ApiErrorCodes를 override 메서드에서 찾을 수 있다") {
            handlerMethod.getMethodAnnotation(ApiErrorCodes::class.java).shouldNotBeNull()
        }

        test("파라미터 레벨 @Parameter는 인터페이스가 아닌 컨트롤러 구현체 파라미터에 직접 선언돼 있다") {
            loginMethod.parameters[0].getAnnotation(Parameter::class.java).shouldNotBeNull()

            val interfaceMethod = OAuthLoginApi::class.java.getMethod("login", String::class.java, OAuthLoginRequest::class.java)
            interfaceMethod.parameters[0].getAnnotation(Parameter::class.java).shouldBeNull()
        }
    })
