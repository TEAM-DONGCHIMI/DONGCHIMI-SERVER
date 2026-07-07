package kr.dongchimi.api.admin.resolver

import kr.dongchimi.api.admin.AdminApiUser
import kr.dongchimi.api.core.common.resolver.RoleApiUserArgumentResolver
import kr.dongchimi.core.auth.PrincipalProvider
import kr.dongchimi.core.auth.Role
import org.springframework.stereotype.Component

@Component
class AdminApiUserArgumentResolver(
    principalProvider: PrincipalProvider,
) : RoleApiUserArgumentResolver<AdminApiUser>(
        principalProvider,
        Role.ADMIN,
        AdminApiUser::class.java,
        ::AdminApiUser,
    )
