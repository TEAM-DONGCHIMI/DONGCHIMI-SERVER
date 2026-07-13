package kr.dongchimi.gateway.auth.security

import kr.dongchimi.core.admin.AdminSignupCodeVerifier
import kr.dongchimi.gateway.auth.config.AdminSignupProperties
import org.springframework.stereotype.Component

@Component
class AdminSignupCodeVerifierImpl(
    private val properties: AdminSignupProperties,
) : AdminSignupCodeVerifier {
    override fun isValid(code: String): Boolean = code == properties.verificationCode
}
