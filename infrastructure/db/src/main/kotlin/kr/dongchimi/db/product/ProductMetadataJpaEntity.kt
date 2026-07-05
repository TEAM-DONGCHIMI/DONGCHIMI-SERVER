package kr.dongchimi.db.product

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import kr.dongchimi.core.product.ProductMetadata

@Entity
@Table(name = "product_metadata")
class ProductMetadataJpaEntity(
    @Id
    @Column(name = "product_id")
    val id: Long,
    @Column(nullable = false)
    val viewCount: Int,
) {
    constructor(productMetadata: ProductMetadata) : this(
        id = productMetadata.id,
        viewCount = productMetadata.viewCount,
    )

    fun toDomain(): ProductMetadata =
        ProductMetadata(
            id = id,
            viewCount = viewCount,
        )
}
