package kr.dongchimi.core.market

import org.springframework.stereotype.Component

@Component
class MarketReader(
    private val marketRepository: MarketRepository,
) {
    fun readByOwnerId(ownerId: Long): Market? = marketRepository.findByOwnerId(ownerId)
}
