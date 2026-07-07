package kr.dongchimi.core.market

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class MarketAppender(
    private val marketRepository: MarketRepository,
) {
    @Transactional
    fun append(
        ownerId: Long,
        info: MarketInfo,
        location: LocationPoint,
        businessHours: BusinessHours?,
        phoneNumber: MarketPhoneNumber,
        brn: String?,
    ): Market =
        marketRepository.save(
            Market(
                ownerId = ownerId,
                info = info,
                location = location,
                businessHours = businessHours,
                phoneNumber = phoneNumber,
                brn = brn,
            ),
        )
}
