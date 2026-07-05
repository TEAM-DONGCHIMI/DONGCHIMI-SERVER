package kr.dongchimi.core.user

import kr.dongchimi.core.auth.AuthErrorCode
import kr.dongchimi.core.auth.SocialUserInfo
import kr.dongchimi.core.common.exception.CoreException
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class UserAppender(
    private val userRepository: UserRepository,
) {
    @Transactional
    fun appendSocialUser(info: SocialUserInfo): User {
        val email = info.email ?: throw CoreException(AuthErrorCode.OAUTH_REQUIRED_INFO_MISSING)
        val gender = info.gender ?: throw CoreException(AuthErrorCode.OAUTH_REQUIRED_INFO_MISSING)

        return userRepository.save(
            User(
                email = email,
                name = info.name,
                socialAccount = info.account,
                gender = gender,
                age = info.age,
            ),
        )
    }
}
