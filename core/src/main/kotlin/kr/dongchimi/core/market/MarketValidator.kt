package kr.dongchimi.core.market

import kr.dongchimi.core.common.exception.CoreException
import org.springframework.stereotype.Component

@Component
class MarketValidator(
    private val marketRepository: MarketRepository,
) {
    fun validateNotDuplicatedOnRegister(
        ownerId: Long,
        name: String,
    ) {
        if (marketRepository.existsByOwnerIdAndName(ownerId, name)) {
            throw CoreException(MarketErrorCode.MARKET_ALREADY_EXISTS)
        }
    }

    fun validateNotDuplicatedOnUpdate(
        ownerId: Long,
        name: String,
        marketId: Long,
    ) {
        if (marketRepository.existsByOwnerIdAndNameAndIdNot(ownerId, name, marketId)) {
            throw CoreException(MarketErrorCode.MARKET_ALREADY_EXISTS)
        }
    }

    fun validateOwnership(
        market: Market,
        ownerId: Long,
    ) {
        if (market.ownerId != ownerId) {
            throw CoreException(MarketErrorCode.MARKET_ACCESS_DENIED)
        }
    }

    fun validateOwnership(
        marketId: Long,
        ownerId: Long,
    ) {
        validateExists(marketId)

        if (!marketRepository.existsByIdAndOwnerId(marketId, ownerId)) {
            throw CoreException(MarketErrorCode.MARKET_ACCESS_DENIED)
        }
    }

    fun validateExists(marketId: Long) {
        if (!marketRepository.existsById(marketId)) {
            throw CoreException(MarketErrorCode.MARKET_NOT_FOUND)
        }
    }
}
