package kr.dongchimi.db.product

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import kr.dongchimi.core.product.DealType
import kr.dongchimi.core.product.DiscountPeriod
import kr.dongchimi.core.product.Price
import kr.dongchimi.core.product.Product
import kr.dongchimi.core.product.ProductCategory
import kr.dongchimi.db.common.BaseSoftDeleteEntity
import java.math.BigDecimal
import java.time.LocalDate

@Entity
@Table(name = "products")
class ProductJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    val id: Long = 0,
    @Column(name = "market_id", nullable = false)
    val marketId: Long,
    @Column(nullable = false)
    val name: String,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val dealType: DealType,
    val thumbnailUrl: String? = null,
    @Column(nullable = false)
    val originalPrice: BigDecimal,
    @Column(nullable = false)
    val discountedPrice: BigDecimal,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val category: ProductCategory,
    val promotionalPhrase: String? = null,
    @Column(nullable = false)
    var discountStartDate: LocalDate,
    @Column(nullable = false)
    var discountEndDate: LocalDate,
) : BaseSoftDeleteEntity() {
    constructor(product: Product) : this(
        id = product.id,
        marketId = product.marketId,
        name = product.name,
        dealType = product.dealType,
        thumbnailUrl = product.thumbnailUrl,
        originalPrice = product.price.originalPrice,
        discountedPrice = product.price.discountedPrice,
        category = product.category,
        promotionalPhrase = product.promotionalPhrase,
        discountStartDate = product.discountPeriod.discountStartDate,
        discountEndDate = product.discountPeriod.discountEndDate,
    )

    fun toDomain(): Product =
        Product(
            id = id,
            marketId = marketId,
            name = name,
            dealType = dealType,
            thumbnailUrl = thumbnailUrl,
            price = Price(originalPrice, discountedPrice),
            category = category,
            promotionalPhrase = promotionalPhrase,
            discountPeriod = DiscountPeriod(discountStartDate, discountEndDate),
        )

    fun updateDiscountPeriod(discountPeriod: DiscountPeriod) {
        discountStartDate = discountPeriod.discountStartDate
        discountEndDate = discountPeriod.discountEndDate
    }
}
