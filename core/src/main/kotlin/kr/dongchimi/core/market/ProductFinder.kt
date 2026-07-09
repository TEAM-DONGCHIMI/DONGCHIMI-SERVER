package kr.dongchimi.core.market

import kr.dongchimi.core.common.CursorSliceResult
import kr.dongchimi.core.product.DealType
import kr.dongchimi.core.product.PeriodicProductSearchCondition
import kr.dongchimi.core.product.Product
import kr.dongchimi.core.product.ProductRepository
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class ProductFinder(
    private val productRepository: ProductRepository,
) {
    fun findPopularActive(
        marketId: Long,
        date: LocalDate,
        limit: Int,
    ): List<Product> = productRepository.findPopularActive(marketId, date, limit)

    fun findLatestActiveByMarketIds(
        marketIds: List<Long>,
        date: LocalDate,
        limitPerMarket: Int,
    ): List<Product> =
        if (marketIds.isEmpty()) {
            emptyList()
        } else {
            productRepository.findLatestActiveByMarketIds(marketIds, date, limitPerMarket)
        }

    fun countActiveByMarketIds(
        marketIds: List<Long>,
        date: LocalDate,
    ): Map<Long, Int> =
        if (marketIds.isEmpty()) {
            emptyMap()
        } else {
            productRepository.countActiveByMarketIds(marketIds, date)
        }

    fun findActiveByCategory(
        marketId: Long,
        dealType: DealType,
        condition: PeriodicProductSearchCondition,
        date: LocalDate,
    ): CursorSliceResult<Product> {
        val products =
            productRepository.findActiveByMarketIdAndDealTypeAndCategory(
                marketId,
                dealType,
                condition,
                date,
                condition.size + 1,
            )
        val content = products.take(condition.size)
        val hasNext = products.size > condition.size

        return CursorSliceResult(
            content = content,
            hasNext = hasNext,
            nextCursor = if (hasNext) content.last().id else null,
        )
    }
}
