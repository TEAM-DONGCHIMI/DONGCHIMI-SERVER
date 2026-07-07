package kr.dongchimi.core.owner

import org.springframework.stereotype.Component

@Component
class OwnerReader(
    private val ownerRepository: OwnerRepository,
) {
    fun existsByEmail(email: String): Boolean = ownerRepository.findByEmail(email) != null

    fun readByEmail(email: String): Owner? = ownerRepository.findByEmail(email)
}
