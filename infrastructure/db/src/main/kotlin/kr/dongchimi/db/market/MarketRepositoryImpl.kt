package kr.dongchimi.db.market

import kr.dongchimi.core.market.Market
import kr.dongchimi.core.market.MarketRepository
import kr.dongchimi.core.market.NearbyMarket
import kr.dongchimi.core.market.NearbyMarketSearchCondition
import org.springframework.stereotype.Repository

@Repository
class MarketRepositoryImpl(
    private val marketJpaRepository: MarketJpaRepository,
) : MarketRepository {
    override fun findById(id: Long): Market? = marketJpaRepository.findByIdAndDeletedAtIsNull(id)?.toDomain()

    override fun findByOwnerId(ownerId: Long): Market? = marketJpaRepository.findByOwnerIdAndDeletedAtIsNull(ownerId)?.toDomain()

    // 프로젝션으로 거리순 id를 먼저 받고, business_hours(JSONB) 매핑을 위해 엔티티를 PK로 다시 읽는다.
    override fun findNearby(
        condition: NearbyMarketSearchCondition,
        limit: Int,
    ): List<NearbyMarket> {
        val projections =
            marketJpaRepository.findNearby(
                lat = condition.origin.latitude,
                lng = condition.origin.longitude,
                radius = condition.radiusMeters,
                cursor = condition.cursorMarketId,
                limit = limit,
            )
        if (projections.isEmpty()) return emptyList()

        val markets =
            marketJpaRepository
                .findAllByIdInAndDeletedAtIsNull(projections.map { it.marketId })
                .associateBy { it.id }

        return projections.mapNotNull { projection ->
            markets[projection.marketId]?.let { NearbyMarket(market = it.toDomain(), slug = projection.slug) }
        }
    }

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

    override fun existsByIdAndOwnerId(
        marketId: Long,
        ownerId: Long,
    ): Boolean = marketJpaRepository.existsByIdAndOwnerIdAndDeletedAtIsNull(marketId, ownerId)

    override fun existsById(id: Long): Boolean = marketJpaRepository.existsByIdAndDeletedAtIsNull(id)
}
