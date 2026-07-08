package kr.dongchimi.core.market

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class FlyerAppender(
    private val flyerRepository: FlyerRepository,
    private val slugGenerator: SlugGenerator,
) {
    @Transactional
    fun publish(marketId: Long): Flyer {
        flyerRepository.findById(marketId)?.let { return it }
        return flyerRepository.save(Flyer(id = marketId, slug = slugGenerator.generate(), qrCode = null))
    }

    @Transactional
    fun updateQrCode(
        flyer: Flyer,
        qrCode: String,
    ): Flyer = flyerRepository.save(flyer.copy(qrCode = qrCode))
}
