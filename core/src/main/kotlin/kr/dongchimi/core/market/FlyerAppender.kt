package kr.dongchimi.core.market

import org.springframework.stereotype.Component

@Component
class FlyerAppender(
    private val flyerRepository: FlyerRepository,
) {
    fun updateQrCode(
        flyer: Flyer,
        qrCode: String,
    ): Flyer = flyerRepository.save(flyer.copy(qrCode = qrCode))
}
