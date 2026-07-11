package kr.dongchimi.core.admin

data class AdminSignupCommand(
    val name: String,
    val email: String,
    val password: String,
    val verificationCode: String,
)
