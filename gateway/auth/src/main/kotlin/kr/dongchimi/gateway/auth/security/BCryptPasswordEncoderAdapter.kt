package kr.dongchimi.gateway.auth.security

import kr.dongchimi.core.auth.PasswordEncoder
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component

@Component
class BCryptPasswordEncoderAdapter : PasswordEncoder {
    private val delegate = BCryptPasswordEncoder()

    override fun encode(rawPassword: String): String = delegate.encode(rawPassword)

    override fun matches(
        rawPassword: String,
        encodedPassword: String,
    ): Boolean = delegate.matches(rawPassword, encodedPassword)
}
