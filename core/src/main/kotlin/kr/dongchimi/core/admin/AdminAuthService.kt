package kr.dongchimi.core.admin

import kr.dongchimi.core.auth.AuthTokenIssuer
import kr.dongchimi.core.auth.PasswordEncoder
import kr.dongchimi.core.auth.Role
import kr.dongchimi.core.common.exception.CoreException
import org.springframework.stereotype.Service

@Service
class AdminAuthService(
    private val adminReader: AdminReader,
    private val adminAppender: AdminAppender,
    private val passwordEncoder: PasswordEncoder,
    private val authTokenIssuer: AuthTokenIssuer,
    private val adminSignupCodeVerifier: AdminSignupCodeVerifier,
) {
    fun signup(command: AdminSignupCommand): AdminAuthResult {
        if (!adminSignupCodeVerifier.isValid(command.verificationCode)) {
            throw CoreException(AdminErrorCode.VERIFICATION_CODE_MISMATCH)
        }
        if (adminReader.existsByEmail(command.email)) {
            throw CoreException(AdminErrorCode.DUPLICATE_EMAIL)
        }

        val admin = adminAppender.append(command)
        val tokens = authTokenIssuer.issue(admin.id, setOf(Role.ADMIN.name))

        return AdminAuthResult(tokens, admin, isAutoLogin = true)
    }

    fun login(command: AdminLoginCommand): AdminAuthResult {
        val admin =
            adminReader.readByEmail(command.email)
                ?: throw CoreException(AdminErrorCode.ADMIN_NOT_FOUND)

        if (!passwordEncoder.matches(command.password, admin.password)) {
            throw CoreException(AdminErrorCode.ADMIN_PASSWORD_MISMATCH)
        }

        val tokens = authTokenIssuer.issue(admin.id, setOf(Role.ADMIN.name))

        return AdminAuthResult(tokens, admin, command.isAutoLogin)
    }
}
