package kr.dongchimi.core.owner

import kr.dongchimi.core.auth.AuthTokenIssuer
import kr.dongchimi.core.auth.PasswordEncoder
import kr.dongchimi.core.auth.Role
import kr.dongchimi.core.common.exception.CoreException
import org.springframework.stereotype.Service

@Service
class OwnerAuthService(
    private val ownerReader: OwnerReader,
    private val ownerAppender: OwnerAppender,
    private val passwordEncoder: PasswordEncoder,
    private val authTokenIssuer: AuthTokenIssuer,
) {
    fun signup(command: OwnerSignupCommand): Owner {
        if (ownerReader.existsByEmail(command.email)) {
            throw CoreException(OwnerErrorCode.DUPLICATE_EMAIL)
        }
        return ownerAppender.append(command)
    }

    fun login(command: OwnerLoginCommand): OwnerAuthResult {
        val owner = ownerReader.readByEmail(command.email)
        val passwordMatches =
            passwordEncoder.matches(command.password, owner?.password ?: DUMMY_PASSWORD_HASH)

        if (owner == null || !passwordMatches) {
            throw CoreException(OwnerErrorCode.LOGIN_FAILED)
        }

        val tokens = authTokenIssuer.issue(owner.id, setOf(Role.OWNER.name))

        return OwnerAuthResult(tokens, owner, command.isAutoLogin)
    }

    companion object {
        private const val DUMMY_PASSWORD_HASH = "\$2a\$10\$abcdefghijklmnopqrstuv0123456789ABCDEFGHIJKLMNOPQRSTU"
    }
}
