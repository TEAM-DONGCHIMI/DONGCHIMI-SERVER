package kr.dongchimi.gateway.auth.security

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority

class UserAuthentication(
    val userId: Long,
    roles: Set<String>,
) : UsernamePasswordAuthenticationToken(
    userId,
    null,
    roles.map { SimpleGrantedAuthority(it) },
)