package kr.dongchimi.core.market

import kr.dongchimi.core.common.exception.CoreException
import org.springframework.stereotype.Component

@Component
class FlyerReader(
    private val flyerRepository: FlyerRepository,
) {
    fun readByMarketId(marketId: Long): Flyer = flyerRepository.findById(marketId) ?: throw CoreException(FlyerErrorCode.FLYER_NOT_FOUND)
}
