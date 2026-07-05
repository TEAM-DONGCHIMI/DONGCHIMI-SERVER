package kr.dongchimi.core.auth

import kr.dongchimi.core.user.UserAppender
import kr.dongchimi.core.user.UserReader
import org.springframework.stereotype.Service

@Service
class OAuthLoginService(
    private val socialUserInfoReader: SocialUserInfoReader,
    private val userReader: UserReader,
    private val userAppender: UserAppender,
    private val tokenProvider: TokenProvider,
) {
    fun login(command: OAuthLoginCommand): String {
        val socialUserInfo = socialUserInfoReader.read(command.provider, command.accessToken)
        val user =
            userReader.readBySocialAccount(socialUserInfo.account)
                ?: userAppender.appendSocialUser(socialUserInfo)

        return tokenProvider.issueAccessToken(user.id, setOf(Role.USER.name))
    }
}
