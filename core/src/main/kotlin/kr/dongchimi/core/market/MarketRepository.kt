package kr.dongchimi.core.market

interface MarketRepository {
    fun findById(id: Long): Market?

    fun save(market: Market): Market

    fun existsByOwnerIdAndName(
        ownerId: Long,
        name: String,
    ): Boolean

    fun existsByOwnerIdAndNameAndIdNot(
        ownerId: Long,
        name: String,
        id: Long,
    ): Boolean

    fun existsByIdAndOwnerId(
        marketId: Long,
        ownerId: Long,
    ): Boolean

    fun existsById(id: Long): Boolean
}
