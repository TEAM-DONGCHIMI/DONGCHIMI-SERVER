package kr.dongchimi.core.admin

import kr.dongchimi.core.auth.PasswordEncoder
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class AdminAppender(
    private val adminRepository: AdminRepository,
    private val passwordEncoder: PasswordEncoder,
) {
    @Transactional
    fun append(command: AdminSignupCommand): Admin =
        adminRepository.save(
            Admin(
                name = command.name,
                email = command.email,
                password = passwordEncoder.encode(command.password),
            ),
        )
}
