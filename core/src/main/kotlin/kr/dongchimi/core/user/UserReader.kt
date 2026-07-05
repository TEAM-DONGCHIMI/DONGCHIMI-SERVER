package kr.dongchimi.core.user

import org.springframework.stereotype.Component

@Component
class UserReader(
    private val userRepository: UserRepository,
) {
    fun readBySocialAccount(account: SocialAccount): User? = userRepository.findBySocialAccount(account)
}
