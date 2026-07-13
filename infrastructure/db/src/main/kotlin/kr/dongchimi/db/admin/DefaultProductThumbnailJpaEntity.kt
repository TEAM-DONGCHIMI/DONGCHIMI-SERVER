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
    ) {
        // 수정(merge) 경로에서 lateinit createdAt이 비어있으면 merge 시 기존 값이 유실될 수 있어 명시적으로 채운다.
        // 신규 생성 경로에서는 @CreatedDate 오디팅이 persist 시점에 이 값을 다시 덮어써 영향 없다.
        createdAt = defaultProductThumbnail.createdAt
    }

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
