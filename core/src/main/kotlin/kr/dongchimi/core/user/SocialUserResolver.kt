package kr.dongchimi.core.user

import kr.dongchimi.core.auth.SocialUserInfo
import kr.dongchimi.core.user.exception.DuplicateSocialAccountException
import org.springframework.stereotype.Component

@Component
class SocialUserResolver(
    private val userReader: UserReader,
    private val userAppender: UserAppender,
) {
    fun resolve(info: SocialUserInfo): User =
        try {
            userAppender.appendSocialUser(info)
        } catch (exception: DuplicateSocialAccountException) {
            userReader.readBySocialAccount(info.account) ?: throw exception
        }
}
