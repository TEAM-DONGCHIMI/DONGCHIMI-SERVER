package kr.dongchimi.core.user

data class User(
    val id: Long = 0,
    val email: String,
    val name: String?,
    val socialProvider: SocialProvider,
    val socialId: String?,
    val gender: Gender,
    val age: Int?,
)
