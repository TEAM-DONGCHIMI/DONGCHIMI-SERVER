package kr.dongchimi.core.auth

import kr.dongchimi.core.common.exception.CoreException
import kr.dongchimi.core.user.SocialProvider
import org.springframework.stereotype.Component

@Component
class SocialUserInfoReader(
    private val oAuthUserClients: List<OAuthUserClient>,
) {
    fun read(
        provider: SocialProvider,
        accessToken: String,
    ): SocialUserInfo {
        val client =
            oAuthUserClients.find { it.provider == provider }
                ?: throw CoreException(AuthErrorCode.UNSUPPORTED_OAUTH_PROVIDER)

        return client.fetchUserInfo(accessToken)
    }
}
