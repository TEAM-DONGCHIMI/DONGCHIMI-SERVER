package kr.dongchimi.db.market

import kr.dongchimi.core.market.MarketMetadata
import kr.dongchimi.core.market.MarketMetadataRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class MarketMetadataRepositoryImpl(
    private val marketMetadataJpaRepository: MarketMetadataJpaRepository,
) : MarketMetadataRepository {
    override fun findById(id: Long): MarketMetadata? = marketMetadataJpaRepository.findByIdOrNull(id)?.toDomain()

    override fun save(marketMetadata: MarketMetadata): MarketMetadata =
        marketMetadataJpaRepository.save(MarketMetadataJpaEntity(marketMetadata)).toDomain()

    @Transactional
    override fun incrementViewCounts(deltas: Map<Long, Int>) {
        // id 오름차순으로 적용해 동시 flush 간 잠금 획득 순서를 일관되게 → 교차 데드락 예방.
        deltas.toSortedMap().forEach { (id, delta) -> marketMetadataJpaRepository.upsertIncrement(id, delta) }
    }
}
