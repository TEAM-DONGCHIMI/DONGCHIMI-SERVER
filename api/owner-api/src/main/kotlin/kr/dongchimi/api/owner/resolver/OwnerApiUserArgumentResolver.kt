package kr.dongchimi.api.owner.resolver

import kr.dongchimi.api.core.common.resolver.RoleApiUserArgumentResolver
import kr.dongchimi.api.owner.OwnerApiUser
import kr.dongchimi.core.auth.PrincipalProvider
import kr.dongchimi.core.auth.Role
import kr.dongchimi.core.owner.OwnerValidator
import org.springframework.stereotype.Component

@Component
class OwnerApiUserArgumentResolver(
    principalProvider: PrincipalProvider,
    ownerValidator: OwnerValidator,
) : RoleApiUserArgumentResolver<OwnerApiUser>(
        principalProvider,
        Role.OWNER,
        OwnerApiUser::class.java,
        ::OwnerApiUser,
        ownerValidator::validate,
    )
