package kr.dongchimi.core.market

import kr.dongchimi.core.common.exception.CoreException
import org.springframework.stereotype.Component

@Component
class MarketReader(
    private val marketRepository: MarketRepository,
) {
    fun read(ownerId: Long): Market = marketRepository.findById(ownerId) ?: throw CoreException(MarketErrorCode.MARKET_NOT_FOUND)
}
