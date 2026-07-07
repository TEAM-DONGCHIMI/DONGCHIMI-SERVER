package kr.dongchimi.api.user.auth

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletResponse
import kr.dongchimi.api.core.auth.RefreshTokenCookieFactory
import kr.dongchimi.api.core.common.swagger.ApiErrorCodes
import kr.dongchimi.api.user.auth.request.OAuthLoginRequest
import kr.dongchimi.core.auth.OAuthLoginService
import org.mockito.Mockito
import org.springframework.core.annotation.AnnotatedElementUtils
import org.springframework.web.method.HandlerMethod

class OAuthLoginApiAnnotationTest :
    FunSpec({
        val controller =
            OAuthLoginController(
                Mockito.mock(OAuthLoginService::class.java),
                Mockito.mock(RefreshTokenCookieFactory::class.java),
            )
        val loginMethod =
            OAuthLoginController::class.java.getMethod(
                "login",
                String::class.java,
                OAuthLoginRequest::class.java,
                HttpServletResponse::class.java,
            )
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
    })
