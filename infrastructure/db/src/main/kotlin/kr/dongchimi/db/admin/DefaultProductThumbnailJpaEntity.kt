package kr.dongchimi.db.admin

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import kr.dongchimi.core.admin.DefaultProductThumbnail
import kr.dongchimi.core.product.ProductCategory
import kr.dongchimi.db.common.BaseCreatedTimeEntity

@Entity
@Table(name = "default_product_thumbnails")
class DefaultProductThumbnailJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dpt_id")
    val id: Long = 0,
    @Column(nullable = false)
    val name: String,
    @Column(nullable = false)
    val thumbnailUrl: String,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val category: ProductCategory,
    @Column(nullable = false)
    val createdBy: Long,
) : BaseCreatedTimeEntity() {
    constructor(defaultProductThumbnail: DefaultProductThumbnail) : this(
        id = defaultProductThumbnail.id,
        name = defaultProductThumbnail.name,
        thumbnailUrl = defaultProductThumbnail.thumbnailUrl,
        category = defaultProductThumbnail.category,
        createdBy = defaultProductThumbnail.createdBy,
    )

    fun toDomain(): DefaultProductThumbnail =
        DefaultProductThumbnail(
            id = id,
            name = name,
            thumbnailUrl = thumbnailUrl,
            category = category,
            createdBy = createdBy,
            createdAt = createdAt,
        )
}
