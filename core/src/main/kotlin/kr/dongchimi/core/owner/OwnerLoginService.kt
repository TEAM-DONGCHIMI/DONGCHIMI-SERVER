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
    fun login(command: OwnerLoginCommand): OwnerLoginResult {
        val owner =
            ownerReader.readByEmail(command.email)
                ?: throw CoreException(OwnerErrorCode.LOGIN_FAILED)

        if (!passwordEncoder.matches(command.password, owner.password)) {
            throw CoreException(OwnerErrorCode.LOGIN_FAILED)
        }

        val market = marketReader.readByOwnerId(owner.id)
        val tokens = authTokenIssuer.issue(owner.id, setOf(Role.OWNER.name))

        return OwnerLoginResult(tokens, owner, market, command.isAutoLogin)
    }
}
