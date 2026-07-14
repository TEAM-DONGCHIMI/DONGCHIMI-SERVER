package kr.dongchimi.api.core.common.resolver

import kr.dongchimi.api.core.common.ApiUser
import kr.dongchimi.core.auth.PrincipalProvider
import kr.dongchimi.core.auth.Role
import kr.dongchimi.core.common.exception.CommonErrorCode
import kr.dongchimi.core.common.exception.CoreException
import org.springframework.core.MethodParameter
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

abstract class RoleApiUserArgumentResolver<T : ApiUser>(
    private val principalProvider: PrincipalProvider,
    private val role: Role,
    private val userType: Class<T>,
    private val factory: (Long, Set<String>) -> T,
    private val validateAccount: (Long) -> Unit,
) : HandlerMethodArgumentResolver {
    override fun supportsParameter(parameter: MethodParameter): Boolean = parameter.parameterType == userType

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?,
    ): T {
        if (role.name !in principalProvider.roles) throw CoreException(CommonErrorCode.FORBIDDEN)

        validateAccount(principalProvider.userId)

        return factory(principalProvider.userId, principalProvider.roles)
    }
}
