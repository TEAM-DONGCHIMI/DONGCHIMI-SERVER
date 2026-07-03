package kr.dongchimi.db.market

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import kr.dongchimi.core.market.Flyer
import kr.dongchimi.db.common.BaseTimeEntity

@Entity
@Table(name = "flyers")
class FlyerJpaEntity(
    @Id
    @Column(name = "market_id")
    val id: Long,
    @Column(nullable = false)
    val slug: String,
    val qrCode: String? = null,
) : BaseTimeEntity() {
    constructor(flyer: Flyer) : this(
        id = flyer.id,
        slug = flyer.slug,
        qrCode = flyer.qrCode,
    )

    fun toDomain(): Flyer =
        Flyer(
            id = id,
            slug = slug,
            qrCode = qrCode,
        )
}
