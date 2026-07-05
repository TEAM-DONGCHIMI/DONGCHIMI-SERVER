package kr.dongchimi.core.auth

import kr.dongchimi.core.user.SocialProvider

interface OAuthUserClient {
    val provider: SocialProvider

    fun fetchUserInfo(accessToken: String): SocialUserInfo
}
