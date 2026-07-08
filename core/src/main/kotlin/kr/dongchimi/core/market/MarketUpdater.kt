package kr.dongchimi.core.market

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class MarketUpdater(
    private val marketRepository: MarketRepository,
) {
    @Transactional
    fun update(
        marketId: Long,
        ownerId: Long,
        info: MarketInfo,
        location: LocationPoint,
        businessHours: BusinessHours,
        phoneNumber: MarketPhoneNumber,
        brn: String?,
    ): Market =
        marketRepository.save(
            Market(
                id = marketId,
                ownerId = ownerId,
                info = info,
                location = location,
                businessHours = businessHours,
                phoneNumber = phoneNumber,
                brn = brn,
            ),
        )
}
