package kr.dongchimi.core.auth

interface PrincipalProvider {
    val userId: Long
    val roles: Set<String>
}
