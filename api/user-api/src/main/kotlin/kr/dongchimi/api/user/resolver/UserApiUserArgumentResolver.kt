package kr.dongchimi.api.user.resolver

import kr.dongchimi.api.user.UserApiUser
import kr.dongchimi.core.auth.PrincipalProvider
import kr.dongchimi.core.auth.Role
import kr.dongchimi.core.common.exception.CommonErrorCode
import kr.dongchimi.core.common.exception.CoreException
import org.springframework.core.MethodParameter
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

@Component
class UserApiUserArgumentResolver(
    private val principalProvider: PrincipalProvider,
) : HandlerMethodArgumentResolver {
    override fun supportsParameter(parameter: MethodParameter): Boolean = parameter.parameterType == UserApiUser::class.java

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?,
    ): UserApiUser {
        if (Role.USER.name !in principalProvider.roles) throw CoreException(CommonErrorCode.FORBIDDEN)

        return UserApiUser(
            principalProvider.userId,
            principalProvider.roles,
        )
    }
}
