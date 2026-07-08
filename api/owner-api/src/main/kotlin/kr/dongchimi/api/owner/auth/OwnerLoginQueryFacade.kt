package kr.dongchimi.api.owner.auth

import kr.dongchimi.api.owner.auth.response.OwnerLoginResponse
import kr.dongchimi.core.market.MarketService
import kr.dongchimi.core.owner.OwnerAuthService
import kr.dongchimi.core.owner.OwnerLoginCommand
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class OwnerLoginQueryFacade(
    private val ownerAuthService: OwnerAuthService,
    private val marketService: MarketService,
) {
    @Transactional
    fun login(command: OwnerLoginCommand): OwnerLoginResult {
        val result = ownerAuthService.login(command)
        val market = marketService.findByOwnerId(result.owner.id)

        val response =
            OwnerLoginResponse(
                accessToken = result.tokens.accessToken,
                ownerId = result.owner.id,
                email = result.owner.email,
                marketId = market?.id,
                marketName = market?.info?.name,
                marketThumbnailUrl = market?.info?.thumbnailUrl,
            )

        return OwnerLoginResult(
            response = response,
            refreshToken = result.tokens.refreshToken,
            refreshExpiresAt = result.tokens.refreshExpiresAt,
            isAutoLogin = result.isAutoLogin,
        )
    }
}
