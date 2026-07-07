package kr.dongchimi.core.owner

import kr.dongchimi.core.auth.PasswordEncoder
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class OwnerAppender(
    private val ownerRepository: OwnerRepository,
    private val passwordEncoder: PasswordEncoder,
) {
    @Transactional
    fun append(command: OwnerSignupCommand): Owner =
        ownerRepository.save(
            Owner(
                email = command.email,
                password = passwordEncoder.encode(command.password),
            ),
        )
}
