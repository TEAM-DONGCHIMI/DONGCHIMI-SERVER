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
    ): String {
        marketValidator.validateOwnership(marketId, ownerId)

        val flyer = flyerAppender.publish(marketId)
        return flyer.slug
    }

    fun issueQrCode(
        ownerId: Long,
        marketId: Long,
    ): FlyerQr {
        marketValidator.validateOwnership(marketId, ownerId)

        val flyer = flyerReader.readByMarketId(marketId)
        val qrCode = flyer.qrCode ?: issueAndStoreQrCode(flyer)
        return FlyerQr(qrCode)
    }

    fun getShareInfo(marketId: Long): FlyerShareInfo {
        val market = marketReader.read(marketId)
        val flyer = flyerReader.readByMarketId(marketId)
        val qrCode = flyer.qrCode ?: issueAndStoreQrCode(flyer)

        return FlyerShareInfo(
            marketId = market.id,
            marketName = market.info.name,
            slug = flyer.slug,
            qrCode = qrCode,
        )
    }

    private fun issueAndStoreQrCode(flyer: Flyer): String {
        val qrCode = flyerQrManager.generate(flyer.slug)
        flyerAppender.updateQrCode(flyer, qrCode)
        return qrCode
    }
}
