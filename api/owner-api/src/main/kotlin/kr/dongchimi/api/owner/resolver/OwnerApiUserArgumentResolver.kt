package kr.dongchimi.api.owner.resolver

import kr.dongchimi.api.owner.OwnerApiUser
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
class OwnerApiUserArgumentResolver(
    private val principalProvider: PrincipalProvider,
) : HandlerMethodArgumentResolver {
    override fun supportsParameter(parameter: MethodParameter): Boolean = parameter.parameterType == OwnerApiUser::class.java

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?,
    ): OwnerApiUser {
        if (Role.OWNER.name !in principalProvider.roles) throw CoreException(CommonErrorCode.FORBIDDEN)

        return OwnerApiUser(
            principalProvider.userId,
            principalProvider.roles,
        )
    }
}
