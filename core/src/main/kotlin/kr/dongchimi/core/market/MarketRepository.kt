package kr.dongchimi.core.market

interface MarketRepository {
    fun findById(id: Long): Market?

    fun save(market: Market): Market
}
