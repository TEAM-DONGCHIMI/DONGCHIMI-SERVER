package kr.dongchimi.core.auth

import kr.dongchimi.core.user.SocialProvider

data class OAuthLoginCommand(
    val provider: SocialProvider,
    val accessToken: String,
)
