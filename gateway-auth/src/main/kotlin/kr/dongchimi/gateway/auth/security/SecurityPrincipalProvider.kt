package kr.dongchimi.gateway.auth.security

import kr.dongchimi.core.auth.PrincipalProvider
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

@Component
class SecurityPrincipalProvider : PrincipalProvider {
    override val userId: Long
        get() = authentication.userId

    override val roles: Set<String>
        get() = authentication.authorities.mapNotNull { it.authority }.toSet()

    private val authentication: UserAuthentication
        get() = SecurityContextHolder.getContext().authentication as UserAuthentication
}
