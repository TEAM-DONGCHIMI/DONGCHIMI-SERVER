package kr.dongchimi.db.market

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface MarketJpaRepository : JpaRepository<MarketJpaEntity, Long> {
    fun findByIdAndDeletedAtIsNull(id: Long): MarketJpaEntity?

    fun findAllByIdInAndDeletedAtIsNull(ids: List<Long>): List<MarketJpaEntity>

    /**
     * 기준 좌표 반경 내 마트를 거리 오름차순으로 조회한다. 전단(flyers)이 발행된 마트만 대상이다.
     *
     * 커서는 마트 id 하나만 받고 그 마트의 거리를 서브쿼리로 다시 계산해
     * (거리, 마트 id) 복합 커서로 비교한다. 존재하지 않는 커서면 서브쿼리가 NULL이 되어 빈 결과가 나온다.
     * cursor는 null이 들어올 수 있어 bytea로 추론되지 않도록 bigint로 캐스팅한다.
     */
    @Query(
        value = """
            SELECT m.market_id AS marketId,
                   f.slug AS slug
            FROM markets m
            JOIN flyers f ON f.market_id = m.market_id
            WHERE m.deleted_at IS NULL
              AND ST_DWithin(m.location, ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography, :radius)
              AND (
                cast(:cursor as bigint) IS NULL
                OR ROW(
                     ST_Distance(m.location, ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography),
                     m.market_id
                   ) > ROW(
                     (SELECT ST_Distance(c.location, ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography)
                        FROM markets c WHERE c.market_id = cast(:cursor as bigint)),
                     cast(:cursor as bigint)
                   )
              )
            ORDER BY ST_Distance(m.location, ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography) ASC,
                     m.market_id ASC
            LIMIT :limit
        """,
        nativeQuery = true,
    )
    fun findNearby(
        @Param("lat") lat: Double,
        @Param("lng") lng: Double,
        @Param("radius") radius: Double,
        @Param("cursor") cursor: Long?,
        @Param("limit") limit: Int,
    ): List<NearbyMarketProjection>

    fun findFirstByOwnerIdAndDeletedAtIsNullOrderByIdAsc(ownerId: Long): MarketJpaEntity?

    fun findByOwnerIdAndDeletedAtIsNull(ownerId: Long): MarketJpaEntity?

    fun existsByOwnerIdAndNameAndDeletedAtIsNull(
        ownerId: Long,
        name: String,
    ): Boolean

    fun existsByOwnerIdAndNameAndIdNotAndDeletedAtIsNull(
        ownerId: Long,
        name: String,
        id: Long,
    ): Boolean

    fun existsByIdAndOwnerIdAndDeletedAtIsNull(
        marketId: Long,
        ownerId: Long,
    ): Boolean

    fun existsByIdAndDeletedAtIsNull(id: Long): Boolean
}
