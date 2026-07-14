package kr.dongchimi.api.owner.auth

import kr.dongchimi.api.owner.auth.response.OwnerLoginResponse
import kr.dongchimi.core.market.MarketService
import kr.dongchimi.core.owner.OwnerAuthResult
import kr.dongchimi.core.owner.OwnerAuthService
import kr.dongchimi.core.owner.OwnerLoginCommand
import kr.dongchimi.core.owner.OwnerSignupCommand
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class OwnerAuthFacade(
    private val ownerAuthService: OwnerAuthService,
    private val marketService: MarketService,
) {
    @Transactional
    fun login(command: OwnerLoginCommand): OwnerLoginResult = toLoginResult(ownerAuthService.login(command))

    @Transactional
    fun signup(command: OwnerSignupCommand): OwnerLoginResult = toLoginResult(ownerAuthService.signup(command))

    private fun toLoginResult(result: OwnerAuthResult): OwnerLoginResult {
        val market = marketService.findByOwnerId(result.owner.id)

        return OwnerLoginResult(
            response = OwnerLoginResponse(result.tokens, result.owner, market),
            refreshToken = result.tokens.refreshToken,
            refreshExpiresAt = result.tokens.refreshExpiresAt,
            isAutoLogin = result.isAutoLogin,
        )
    }
}
