package kr.dongchimi.core.owner

import kr.dongchimi.core.auth.AuthTokenIssuer
import kr.dongchimi.core.auth.PasswordEncoder
import kr.dongchimi.core.auth.Role
import kr.dongchimi.core.common.exception.CoreException
import kr.dongchimi.core.market.MarketReader
import org.springframework.stereotype.Service

@Service
class OwnerLoginService(
    private val ownerReader: OwnerReader,
    private val marketReader: MarketReader,
    private val passwordEncoder: PasswordEncoder,
    private val authTokenIssuer: AuthTokenIssuer,
) {
    companion object {
        // BCrypt 형식의 더미 해시로, 실제 사용자 비밀번호와 무관
        private const val DUMMY_PASSWORD_HASH = "\$2a\$10\$abcdefghijklmnopqrstuv0123456789ABCDEFGHIJKLMNOPQRSTU"
    }

    fun login(command: OwnerLoginCommand): OwnerLoginResult {
        val owner = ownerReader.readByEmail(command.email)
        val passwordMatches =
            passwordEncoder.matches(command.password, owner?.password ?: DUMMY_PASSWORD_HASH)

        if (owner == null || !passwordMatches) {
            throw CoreException(OwnerErrorCode.LOGIN_FAILED)
        }

        val market = marketReader.readByOwnerId(owner.id)
        val tokens = authTokenIssuer.issue(owner.id, setOf(Role.OWNER.name))

        return OwnerLoginResult(tokens, owner, market, command.isAutoLogin)
    }
}
