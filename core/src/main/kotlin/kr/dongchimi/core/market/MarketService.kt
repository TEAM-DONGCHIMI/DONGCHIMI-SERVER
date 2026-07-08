package kr.dongchimi.core.market

import org.springframework.stereotype.Service

@Service
class MarketService(
    private val marketReader: MarketReader,
) {
    fun findByOwnerId(ownerId: Long): Market? = marketReader.readByOwnerId(ownerId)
}
