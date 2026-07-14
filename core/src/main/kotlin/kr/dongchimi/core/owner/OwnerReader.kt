package kr.dongchimi.core.owner

import kr.dongchimi.core.auth.AccountCache
import kr.dongchimi.core.auth.Role
import org.springframework.stereotype.Component

@Component
class OwnerReader(
    private val ownerRepository: OwnerRepository,
    private val accountCache: AccountCache,
) {
    fun existsByEmail(email: String): Boolean = ownerRepository.existsByEmail(email)

    fun readByEmail(email: String): Owner? = ownerRepository.findByEmail(email)

    /** 1) 캐시 hit → true(DB 미조회) 2) miss → DB. 존재하면 캐시 기록 후 true, 없으면 false(미캐싱). */
    fun existsById(ownerId: Long): Boolean {
        if (accountCache.isKnownToExist(Role.OWNER, ownerId)) return true

        val exists = ownerRepository.existsById(ownerId)
        if (exists) accountCache.markExists(Role.OWNER, ownerId)
        return exists
    }
}
