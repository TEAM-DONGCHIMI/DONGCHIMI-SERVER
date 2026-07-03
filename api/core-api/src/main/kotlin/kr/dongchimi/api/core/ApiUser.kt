package kr.dongchimi.api.core

interface ApiUser {
    val userId: Long
    val roles: Set<String>
}
