package kr.dongchimi.db.market

import kr.dongchimi.core.market.Market
import kr.dongchimi.core.market.MarketRepository
import org.springframework.stereotype.Repository

@Repository
class MarketRepositoryImpl(
    private val marketJpaRepository: MarketJpaRepository,
) : MarketRepository {
    override fun findById(id: Long): Market? = marketJpaRepository.findByIdAndDeletedAtIsNull(id)?.toDomain()

    override fun save(market: Market): Market = marketJpaRepository.save(MarketJpaEntity(market)).toDomain()

    override fun existsByOwnerIdAndName(
        ownerId: Long,
        name: String,
    ): Boolean = marketJpaRepository.existsByOwnerIdAndNameAndDeletedAtIsNull(ownerId, name)

    override fun existsByOwnerIdAndNameAndIdNot(
        ownerId: Long,
        name: String,
        id: Long,
    ): Boolean = marketJpaRepository.existsByOwnerIdAndNameAndIdNotAndDeletedAtIsNull(ownerId, name, id)
}
