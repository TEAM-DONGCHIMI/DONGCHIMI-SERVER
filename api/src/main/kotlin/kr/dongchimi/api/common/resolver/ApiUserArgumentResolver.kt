package kr.dongchimi.api.common.resolver

import kr.dongchimi.api.common.ApiUser
import kr.dongchimi.core.auth.PrincipalProvider
import org.springframework.core.MethodParameter
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

@Component
class ApiUserArgumentResolver(
    private val principalProvider: PrincipalProvider,
) : HandlerMethodArgumentResolver {

    override fun supportsParameter(parameter: MethodParameter): Boolean =
        parameter.parameterType == ApiUser::class.java

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?,
    ): ApiUser {
        return ApiUser(
            principalProvider.userId,
            principalProvider.roles
        )
    }
}
