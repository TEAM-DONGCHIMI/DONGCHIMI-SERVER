package kr.dongchimi.db.market

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import kr.dongchimi.core.market.MarketMetadata

@Entity
@Table(name = "market_metadata")
class MarketMetadataJpaEntity(
    @Id
    @Column(name = "market_id")
    val id: Long,
    @Column(nullable = false)
    val viewCount: Int,
) {
    constructor(marketMetadata: MarketMetadata) : this(
        id = marketMetadata.id,
        viewCount = marketMetadata.viewCount,
    )

    fun toDomain(): MarketMetadata =
        MarketMetadata(
            id = id,
            viewCount = viewCount,
        )
}
