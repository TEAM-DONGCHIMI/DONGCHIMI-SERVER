package kr.dongchimi.core.owner

import kr.dongchimi.core.auth.PasswordEncoder
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class PendingOwnerSignupManager(
    private val pendingOwnerSignupStore: PendingOwnerSignupStore,
    private val passwordEncoder: PasswordEncoder,
) {
    fun append(command: OwnerSignupCommand): String {
        val signupToken = UUID.randomUUID().toString()
        pendingOwnerSignupStore.save(
            signupToken,
            PendingOwnerSignup(
                email = command.email,
                encodedPassword = passwordEncoder.encode(command.password),
            ),
        )
        return signupToken
    }

    fun find(signupToken: String): PendingOwnerSignup? = pendingOwnerSignupStore.find(signupToken)

    fun remove(signupToken: String) = pendingOwnerSignupStore.delete(signupToken)
}
