package kr.dongchimi.core.market

import kr.dongchimi.core.common.CursorSliceResult
import kr.dongchimi.core.common.exception.CoreException
import org.springframework.stereotype.Service

@Service
class MarketService(
    private val marketReader: MarketReader,
    private val marketFinder: MarketFinder,
    private val marketAppender: MarketAppender,
    private val marketUpdater: MarketUpdater,
    private val marketValidator: MarketValidator,
    private val flyerReader: FlyerReader,
) {
    fun findByOwnerId(ownerId: Long): Market? = marketReader.readByOwnerId(ownerId)

    fun getById(marketId: Long): Market = marketReader.read(marketId)

    fun getNearbyMarkets(condition: NearbyMarketSearchCondition): CursorSliceResult<NearbyMarket> = marketFinder.findNearby(condition)

    fun getBySlug(slug: String): Market {
        val flyer = flyerReader.findBySlug(slug) ?: throw CoreException(MarketErrorCode.MARKET_NOT_FOUND)
        return marketReader.read(flyer.id)
    }

    fun register(
        ownerId: Long,
        command: MarketRegisterCommand,
    ): Market {
        marketValidator.validateNotDuplicatedOnRegister(ownerId, command.info.name)

        return marketAppender.append(
            ownerId = ownerId,
            info = command.info,
            location = command.location,
            businessHours = command.businessHours,
            phoneNumber = command.phoneNumber,
            brn = command.brn,
        )
    }

    fun update(
        ownerId: Long,
        marketId: Long,
        command: MarketUpdateCommand,
    ): Market {
        val market = marketReader.read(marketId)
        marketValidator.validateOwnership(market, ownerId)
        marketValidator.validateNotDuplicatedOnUpdate(ownerId, command.info.name, marketId)

        return marketUpdater.update(
            marketId = marketId,
            ownerId = ownerId,
            info = command.info,
            location = command.location,
            businessHours = command.businessHours,
            phoneNumber = command.phoneNumber,
            brn = command.brn,
        )
    }
}
