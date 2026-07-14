package kr.dongchimi.api.user.resolver

import kr.dongchimi.api.core.common.resolver.RoleApiUserArgumentResolver
import kr.dongchimi.api.user.UserApiUser
import kr.dongchimi.core.auth.PrincipalProvider
import kr.dongchimi.core.auth.Role
import kr.dongchimi.core.user.UserValidator
import org.springframework.stereotype.Component

@Component
class UserApiUserArgumentResolver(
    principalProvider: PrincipalProvider,
    userValidator: UserValidator,
) : RoleApiUserArgumentResolver<UserApiUser>(
        principalProvider,
        Role.USER,
        UserApiUser::class.java,
        ::UserApiUser,
        userValidator::validate,
    )
