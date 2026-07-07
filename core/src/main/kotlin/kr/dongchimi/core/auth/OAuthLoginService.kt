package kr.dongchimi.core.auth

import kr.dongchimi.core.user.SocialUserResolver
import kr.dongchimi.core.user.UserReader
import org.springframework.stereotype.Service

@Service
class OAuthLoginService(
    private val socialUserInfoReader: SocialUserInfoReader,
    private val userReader: UserReader,
    private val socialUserResolver: SocialUserResolver,
    private val authTokenIssuer: AuthTokenIssuer,
) {
    fun login(command: OAuthLoginCommand): AuthTokens {
        val socialUserInfo = socialUserInfoReader.read(command.provider, command.accessToken)
        val user =
            userReader.readBySocialAccount(socialUserInfo.account)
                ?: socialUserResolver.resolve(socialUserInfo)

        return authTokenIssuer.issue(user.id, setOf(Role.USER.name))
    }
}
