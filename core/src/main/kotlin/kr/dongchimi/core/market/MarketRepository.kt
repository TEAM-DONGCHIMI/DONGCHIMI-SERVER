package kr.dongchimi.core.market

interface MarketRepository {
    fun findById(id: Long): Market?

    fun findByOwnerId(ownerId: Long): Market?

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
}
