package kr.dongchimi.db.market

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import kr.dongchimi.core.market.BusinessHours
import kr.dongchimi.core.market.LocationPoint
import kr.dongchimi.core.market.Market
import kr.dongchimi.core.market.MarketPhoneNumber
import kr.dongchimi.db.common.BaseSoftDeleteEntity
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.Point
import org.locationtech.jts.geom.PrecisionModel

private val geometryFactory = GeometryFactory(PrecisionModel(), 4326)

@Entity
@Table(name = "markets")
class MarketJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "market_id")
    val id: Long = 0,
    @Column(name = "owner_id", nullable = false)
    val ownerId: Long,
    @Column(nullable = false)
    val name: String,
    @Column(nullable = false)
    val address: String,
    val thumbnailUrl: String? = null,
    @Column(columnDefinition = "geography(Point,4326)", nullable = false)
    val location: Point,
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    val businessHours: BusinessHours? = null,
    @Column(name = "market_phone_1", nullable = false)
    val marketPhone1: String,
    @Column(name = "market_phone_2")
    val marketPhone2: String? = null,
    @Column(nullable = false)
    val marketPhonePrimary: Short,
    @Column(nullable = false)
    val ownerPhone: String,
    @Column(name = "owner_phone_2")
    val ownerPhone2: String? = null,
    val ownerPhonePrimary: Short? = null,
    val brn: String? = null,
) : BaseSoftDeleteEntity() {
    constructor(market: Market) : this(
        id = market.id,
        ownerId = market.ownerId,
        name = market.name,
        address = market.address,
        thumbnailUrl = market.thumbnailUrl,
        location = geometryFactory.createPoint(Coordinate(market.location.longitude, market.location.latitude)),
        businessHours = market.businessHours,
        marketPhone1 = market.phoneNumber.marketPhone1,
        marketPhone2 = market.phoneNumber.marketPhone2,
        marketPhonePrimary = market.phoneNumber.marketPhonePrimary,
        ownerPhone = market.phoneNumber.ownerPhone,
        ownerPhone2 = market.phoneNumber.ownerPhone2,
        ownerPhonePrimary = market.phoneNumber.ownerPhonePrimary,
        brn = market.brn,
    )

    fun toDomain(): Market =
        Market(
            id = id,
            ownerId = ownerId,
            name = name,
            address = address,
            thumbnailUrl = thumbnailUrl,
            location = LocationPoint(longitude = location.x, latitude = location.y),
            businessHours = businessHours,
            phoneNumber =
                MarketPhoneNumber(
                    marketPhone1,
                    marketPhone2,
                    marketPhonePrimary,
                    ownerPhone,
                    ownerPhone2,
                    ownerPhonePrimary,
                ),
            brn = brn,
        )
}
