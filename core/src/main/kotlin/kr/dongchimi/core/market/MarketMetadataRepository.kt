package kr.dongchimi.core.market

interface MarketMetadataRepository {
    fun findById(id: Long): MarketMetadata?

    fun save(marketMetadata: MarketMetadata): MarketMetadata
}
