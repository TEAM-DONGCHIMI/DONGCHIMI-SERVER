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
import kr.dongchimi.core.product.DraftFailReason
import kr.dongchimi.core.product.DraftStatus
import kr.dongchimi.core.product.PreparedProduct
import kr.dongchimi.core.product.PreparedProductDraftSaveCommand
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
    var name: String? = null,
    var thumbnailUrl: String? = null,
    var originalPrice: BigDecimal? = null,
    var discountedPrice: BigDecimal? = null,
    @Enumerated(EnumType.STRING)
    var category: ProductCategory? = null,
    var promotionalPhrase: String? = null,
    var discountStartDate: LocalDate? = null,
    var discountEndDate: LocalDate? = null,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var dealType: DealType = DealType.PERIODIC,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var draftStatus: DraftStatus,
    @Enumerated(EnumType.STRING)
    var failReason: DraftFailReason? = null,
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
        dealType = preparedProduct.dealType,
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
            dealType = dealType,
            draftStatus = draftStatus,
            failReason = failReason,
        )
    }

    /**
     * [draftStatus]는 [failReason]에서 유도한다. 둘을 따로 받으면 SUCCESS인데 사유가 있는 모순 상태가 생긴다.
     */
    fun update(
        command: PreparedProductDraftSaveCommand,
        failReason: DraftFailReason?,
    ) {
        name = command.name
        thumbnailUrl = command.thumbnailUrl
        originalPrice = command.price?.originalPrice
        discountedPrice = command.price?.discountedPrice
        category = command.category
        promotionalPhrase = command.promotionalPhrase
        discountStartDate = command.discountPeriod?.discountStartDate
        discountEndDate = command.discountPeriod?.discountEndDate
        dealType = command.dealType
        this.failReason = failReason
        draftStatus = if (failReason == null) DraftStatus.SUCCESS else DraftStatus.FAIL
    }
}
