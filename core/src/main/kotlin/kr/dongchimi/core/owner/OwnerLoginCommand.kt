package kr.dongchimi.core.owner

data class OwnerLoginCommand(
    val email: String,
    val password: String,
    val isAutoLogin: Boolean,
)
