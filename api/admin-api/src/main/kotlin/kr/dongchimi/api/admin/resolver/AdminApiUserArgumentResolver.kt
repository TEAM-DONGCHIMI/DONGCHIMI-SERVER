package kr.dongchimi.api.admin.resolver

import kr.dongchimi.api.admin.AdminApiUser
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
class AdminApiUserArgumentResolver(
    private val principalProvider: PrincipalProvider,
) : HandlerMethodArgumentResolver {
    override fun supportsParameter(parameter: MethodParameter): Boolean = parameter.parameterType == AdminApiUser::class.java

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?,
    ): AdminApiUser {
        if (Role.ADMIN.name !in principalProvider.roles) throw CoreException(CommonErrorCode.FORBIDDEN)

        return AdminApiUser(
            principalProvider.userId,
            principalProvider.roles,
        )
    }
}
