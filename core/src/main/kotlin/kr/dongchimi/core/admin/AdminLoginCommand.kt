package kr.dongchimi.core.admin

data class AdminLoginCommand(
    val email: String,
    val password: String,
    val isAutoLogin: Boolean,
)
