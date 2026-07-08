package kr.dongchimi.core.market

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class FlyerAppender(
    private val flyerRepository: FlyerRepository,
) {
    @Transactional
    fun updateQrCode(
        flyer: Flyer,
        qrCode: String,
    ): Flyer = flyerRepository.save(flyer.copy(qrCode = qrCode))
}
