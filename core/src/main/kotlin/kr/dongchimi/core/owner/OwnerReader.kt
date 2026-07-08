package kr.dongchimi.core.owner

import org.springframework.stereotype.Component

@Component
class OwnerReader(
    private val ownerRepository: OwnerRepository,
) {
    fun existsByEmail(email: String): Boolean = ownerRepository.existsByEmail(email)

    fun readByEmail(email: String): Owner? = ownerRepository.findByEmail(email)
}
