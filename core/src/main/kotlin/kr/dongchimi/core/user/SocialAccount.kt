package kr.dongchimi.core.user

data class SocialAccount(
    val provider: SocialProvider,
    val socialId: String,
)
