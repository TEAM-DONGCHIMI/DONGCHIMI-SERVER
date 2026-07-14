package kr.dongchimi.core.user

import kr.dongchimi.core.auth.AccountCache
import kr.dongchimi.core.auth.Role
import org.springframework.stereotype.Component

@Component
class UserReader(
    private val userRepository: UserRepository,
    private val accountCache: AccountCache,
) {
    fun readBySocialAccount(account: SocialAccount): User? = userRepository.findBySocialAccount(account)

    /** 1) 캐시 hit → true(DB 미조회) 2) miss → DB. 존재하면 캐시 기록 후 true, 없으면 false(미캐싱). */
    fun existsById(userId: Long): Boolean {
        if (accountCache.isKnownToExist(Role.USER, userId)) return true

        val exists = userRepository.existsById(userId)
        if (exists) accountCache.markExists(Role.USER, userId)
        return exists
    }
}
