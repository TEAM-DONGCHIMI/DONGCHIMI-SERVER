package kr.dongchimi.core.auth

import kr.dongchimi.core.user.Gender
import kr.dongchimi.core.user.SocialAccount

data class SocialUserInfo(
    val account: SocialAccount,
    val email: String?,
    val name: String?,
    val gender: Gender?,
    val age: Int?,
)
