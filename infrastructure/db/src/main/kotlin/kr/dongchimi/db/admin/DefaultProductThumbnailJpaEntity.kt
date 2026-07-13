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
import kr.dongchimi.db.common.BaseTimeEntity

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
) : BaseTimeEntity() {
    constructor(defaultProductThumbnail: DefaultProductThumbnail) : this(
        id = defaultProductThumbnail.id,
        name = defaultProductThumbnail.name,
        thumbnailUrl = defaultProductThumbnail.thumbnailUrl,
        category = defaultProductThumbnail.category,
        createdBy = defaultProductThumbnail.createdBy,
    ) {
        // merge(update) 경로에서 lateinit createdAt/updatedAt이 비어있으면 save() 반환값을
        // toDomain()으로 변환할 때(auditing이 아직 flush 전이라 값을 못 채운 시점) 값이 유실되거나
        // UninitializedPropertyAccessException이 날 수 있어 명시적으로 채운다.
        // 실제 최종값은 @CreatedDate/@LastModifiedDate 오디팅이 flush 시점에 다시 채운다
        // (신규 생성은 즉시, 수정은 트랜잭션 커밋 시점) — 여기 값은 그 전까지의 임시값일 뿐이다.
        createdAt = defaultProductThumbnail.createdAt
        updatedAt = defaultProductThumbnail.updatedAt
    }

    fun toDomain(): DefaultProductThumbnail =
        DefaultProductThumbnail(
            id = id,
            name = name,
            thumbnailUrl = thumbnailUrl,
            category = category,
            createdBy = createdBy,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
}
