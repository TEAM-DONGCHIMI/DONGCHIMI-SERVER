package kr.dongchimi.core.owner

data class PendingOwnerSignup(
    val email: String,
    val encodedPassword: String,
)
