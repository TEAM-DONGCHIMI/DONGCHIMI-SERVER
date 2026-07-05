package kr.dongchimi.core.user

import kr.dongchimi.core.auth.SocialUserInfo
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Component

@Component
class SocialUserResolver(
    private val userReader: UserReader,
    private val userAppender: UserAppender,
) {
    fun resolve(info: SocialUserInfo): User =
        try {
            userAppender.appendSocialUser(info)
        } catch (exception: DataIntegrityViolationException) {
            userReader.readBySocialAccount(info.account) ?: throw exception
        }
}
