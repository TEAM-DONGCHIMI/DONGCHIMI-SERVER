package kr.dongchimi.api.common

data class ApiUser(
    val userId: Long,
    val roles: Set<String>,
)
