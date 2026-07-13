package kr.dongchimi.db.product

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface ProductMetadataJpaRepository : JpaRepository<ProductMetadataJpaEntity, Long> {
    /** 앱에서 read-modify-write 하지 않고 SQL 안에서 원자적으로 증가시켜 lost update를 막는다. */
    @Modifying
    @Query(
        value = """
            INSERT INTO product_metadata (product_id, view_count) VALUES (:id, :delta)
            ON CONFLICT (product_id) DO UPDATE SET view_count = product_metadata.view_count + :delta
        """,
        nativeQuery = true,
    )
    fun upsertIncrement(
        @Param("id") id: Long,
        @Param("delta") delta: Int,
    )
}
