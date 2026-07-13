package kr.dongchimi.core.owner

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class OwnerAppender(
    private val ownerRepository: OwnerRepository,
) {
    @Transactional
    fun append(
        email: String,
        encodedPassword: String,
    ): Owner =
        ownerRepository.save(
            Owner(
                email = email,
                password = encodedPassword,
            ),
        )
}
