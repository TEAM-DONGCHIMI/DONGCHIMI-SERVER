package kr.dongchimi.db.product

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import kr.dongchimi.core.product.DiscountPeriod
import kr.dongchimi.core.product.DraftStatus
import kr.dongchimi.core.product.PreparedProduct
import kr.dongchimi.core.product.Price
import kr.dongchimi.core.product.ProductCategory
import kr.dongchimi.db.common.BaseSoftDeleteEntity
import java.math.BigDecimal
import java.time.LocalDate

@Entity
@Table(name = "prepared_products")
class PreparedProductJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pp_id")
    val id: Long = 0,
    @Column(name = "market_id", nullable = false)
    val marketId: Long,
    val name: String? = null,
    val thumbnailUrl: String? = null,
    val originalPrice: BigDecimal? = null,
    val discountedPrice: BigDecimal? = null,
    @Enumerated(EnumType.STRING)
    val category: ProductCategory? = null,
    val promotionalPhrase: String? = null,
    val discountStartDate: LocalDate? = null,
    val discountEndDate: LocalDate? = null,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val draftStatus: DraftStatus,
    val failReason: String? = null,
) : BaseSoftDeleteEntity() {
    constructor(preparedProduct: PreparedProduct) : this(
        id = preparedProduct.id,
        marketId = preparedProduct.marketId,
        name = preparedProduct.name,
        thumbnailUrl = preparedProduct.thumbnailUrl,
        originalPrice = preparedProduct.price?.originalPrice,
        discountedPrice = preparedProduct.price?.discountedPrice,
        category = preparedProduct.category,
        promotionalPhrase = preparedProduct.promotionalPhrase,
        discountStartDate = preparedProduct.discountPeriod?.discountStartDate,
        discountEndDate = preparedProduct.discountPeriod?.discountEndDate,
        draftStatus = preparedProduct.draftStatus,
        failReason = preparedProduct.failReason,
    )

    fun toDomain(): PreparedProduct {
        val originalPrice = originalPrice
        val discountedPrice = discountedPrice
        val discountStartDate = discountStartDate
        val discountEndDate = discountEndDate
        return PreparedProduct(
            id = id,
            marketId = marketId,
            name = name,
            thumbnailUrl = thumbnailUrl,
            price = if (originalPrice != null && discountedPrice != null) Price(originalPrice, discountedPrice) else null,
            category = category,
            promotionalPhrase = promotionalPhrase,
            discountPeriod =
                if (discountStartDate != null && discountEndDate != null) {
                    DiscountPeriod(discountStartDate, discountEndDate)
                } else {
                    null
                },
            draftStatus = draftStatus,
            failReason = failReason,
        )
    }
}
