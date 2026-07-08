package kr.dongchimi.core.market

import org.springframework.stereotype.Service

@Service
class FlyerService(
    private val marketReader: MarketReader,
    private val marketValidator: MarketValidator,
    private val flyerReader: FlyerReader,
    private val flyerQrManager: FlyerQrManager,
    private val flyerAppender: FlyerAppender,
) {
    fun publish(
        ownerId: Long,
        marketId: Long,
    ): FlyerPublish {
        val market = marketReader.read(marketId)
        marketValidator.validateOwnership(market, ownerId)

        val flyer = flyerAppender.publish(marketId)
        return FlyerPublish(flyer.slug)
    }

    fun issueQrCode(
        ownerId: Long,
        marketId: Long,
    ): FlyerQr {
        val market = marketReader.read(marketId)
        marketValidator.validateOwnership(market, ownerId)

        val flyer = flyerReader.readByMarketId(marketId)
        flyer.qrCode?.let { return FlyerQr(it) }

        val qrCode = flyerQrManager.generate(flyer.slug)
        flyerAppender.updateQrCode(flyer, qrCode)
        return FlyerQr(qrCode)
    }
}
