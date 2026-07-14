package kr.dongchimi.api.owner.auth

import kr.dongchimi.api.owner.auth.response.OwnerLoginResponse
import kr.dongchimi.core.market.MarketRegisterCommand
import kr.dongchimi.core.owner.OwnerAuthService
import org.springframework.stereotype.Component

@Component
class OwnerSignupCompletionFacade(
    private val ownerAuthService: OwnerAuthService,
) {
    fun complete(
        signupToken: String,
        marketCommand: MarketRegisterCommand,
    ): OwnerLoginResult {
        val result = ownerAuthService.completeSignup(signupToken, marketCommand)

        val response = OwnerLoginResponse(result.tokens.accessToken, result.owner, result.market)

        return OwnerLoginResult(
            response = response,
            refreshToken = result.tokens.refreshToken,
            refreshExpiresAt = result.tokens.refreshExpiresAt,
            isAutoLogin = true,
        )
    }
}
