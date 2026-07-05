package kr.dongchimi.api.user.resolver

import kr.dongchimi.api.core.resolver.RoleApiUserArgumentResolver
import kr.dongchimi.api.user.UserApiUser
import kr.dongchimi.core.auth.PrincipalProvider
import kr.dongchimi.core.auth.Role
import org.springframework.stereotype.Component

@Component
class UserApiUserArgumentResolver(
    principalProvider: PrincipalProvider,
) : RoleApiUserArgumentResolver<UserApiUser>(
        principalProvider,
        Role.USER,
        UserApiUser::class.java,
        ::UserApiUser,
    )
