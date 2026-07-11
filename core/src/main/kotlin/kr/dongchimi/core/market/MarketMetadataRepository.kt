package kr.dongchimi.core.market

interface MarketMetadataRepository {
    fun findById(id: Long): MarketMetadata?

    fun save(marketMetadata: MarketMetadata): MarketMetadata

    /** 마트별 조회수를 원자적으로 증가시킨다. 행이 없으면 생성한다(upsert). */
    fun incrementViewCounts(deltas: Map<Long, Int>)
}
