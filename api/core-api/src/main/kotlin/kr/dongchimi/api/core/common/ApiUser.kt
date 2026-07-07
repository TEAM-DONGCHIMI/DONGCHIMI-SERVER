package kr.dongchimi.api.core.common

interface ApiUser {
    val userId: Long
    val roles: Set<String>
}
