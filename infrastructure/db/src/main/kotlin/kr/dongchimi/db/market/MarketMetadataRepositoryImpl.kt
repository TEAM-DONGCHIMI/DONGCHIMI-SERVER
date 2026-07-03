package kr.dongchimi.db.market

import kr.dongchimi.core.market.MarketMetadata
import kr.dongchimi.core.market.MarketMetadataRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@Repository
class MarketMetadataRepositoryImpl(
    private val marketMetadataJpaRepository: MarketMetadataJpaRepository,
) : MarketMetadataRepository {
    override fun findById(id: Long): MarketMetadata? = marketMetadataJpaRepository.findByIdOrNull(id)?.toDomain()

    override fun save(marketMetadata: MarketMetadata): MarketMetadata =
        marketMetadataJpaRepository.save(MarketMetadataJpaEntity(marketMetadata)).toDomain()
}
