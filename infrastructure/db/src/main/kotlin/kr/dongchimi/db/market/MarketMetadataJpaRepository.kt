package kr.dongchimi.db.market

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface MarketMetadataJpaRepository : JpaRepository<MarketMetadataJpaEntity, Long> {
    /** 앱에서 read-modify-write 하지 않고 SQL 안에서 원자적으로 증가시켜 lost update를 막는다. */
    @Modifying
    @Query(
        value = """
            INSERT INTO market_metadata (market_id, view_count) VALUES (:id, :delta)
            ON CONFLICT (market_id) DO UPDATE SET view_count = market_metadata.view_count + :delta
        """,
        nativeQuery = true,
    )
    fun upsertIncrement(
        @Param("id") id: Long,
        @Param("delta") delta: Int,
    )
}
