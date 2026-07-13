package kr.dongchimi.core.owner

import kr.dongchimi.core.auth.AuthTokenIssuer
import kr.dongchimi.core.auth.PasswordEncoder
import kr.dongchimi.core.auth.Role
import kr.dongchimi.core.common.exception.CoreException
import kr.dongchimi.core.market.MarketRegisterCommand
import org.springframework.stereotype.Service

@Service
class OwnerAuthService(
    private val ownerReader: OwnerReader,
    private val passwordEncoder: PasswordEncoder,
    private val authTokenIssuer: AuthTokenIssuer,
    private val pendingOwnerSignupManager: PendingOwnerSignupManager,
    private val ownerSignupFinisher: OwnerSignupFinisher,
) {
    fun signup(command: OwnerSignupCommand): String {
        if (ownerReader.existsByEmail(command.email)) {
            throw CoreException(OwnerErrorCode.DUPLICATE_EMAIL)
        }
        return pendingOwnerSignupManager.append(command)
    }

    fun completeSignup(
        signupToken: String,
        marketCommand: MarketRegisterCommand,
    ): OwnerSignupCompletionResult {
        val pending =
            pendingOwnerSignupManager.find(signupToken)
                ?: throw CoreException(OwnerErrorCode.SIGNUP_SESSION_NOT_FOUND)

        val completion = ownerSignupFinisher.finish(pending, marketCommand)
        pendingOwnerSignupManager.remove(signupToken)

        val tokens = authTokenIssuer.issue(completion.owner.id, setOf(Role.OWNER.name))
        return OwnerSignupCompletionResult(tokens, completion.owner, completion.market)
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
