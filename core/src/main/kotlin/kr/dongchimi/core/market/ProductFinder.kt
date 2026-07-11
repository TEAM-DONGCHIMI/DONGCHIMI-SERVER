package kr.dongchimi.core.market

import kr.dongchimi.common.utils.HangulUtils.extractChosung
import kr.dongchimi.common.utils.HangulUtils.isChosungOnly
import kr.dongchimi.core.common.CursorSliceResult
import kr.dongchimi.core.common.toCursorSlice
import kr.dongchimi.core.product.DealType
import kr.dongchimi.core.product.PeriodicProductSearchCondition
import kr.dongchimi.core.product.Product
import kr.dongchimi.core.product.ProductKeywordSearchCondition
import kr.dongchimi.core.product.ProductListCursorAnchor
import kr.dongchimi.core.product.ProductListItem
import kr.dongchimi.core.product.ProductListSearchCondition
import kr.dongchimi.core.product.ProductRepository
import kr.dongchimi.core.product.ProductSortType
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
    ): CursorSliceResult<Product> =
        productRepository
            .findActiveByMarketIdAndDealTypeAndCategory(marketId, dealType, condition, date, condition.size + 1)
            .toCursorSlice(condition.size) { it.id }

    fun searchByKeyword(
        marketId: Long,
        condition: ProductKeywordSearchCondition,
        date: LocalDate,
    ): List<Product> =
        if (condition.keyword.isChosungOnly()) {
            productRepository
                .findAllActiveByMarketId(marketId, date)
                .filter { it.name.extractChosung().contains(condition.keyword) }
                .take(condition.size)
        } else {
            productRepository.searchActiveByMarketIdAndKeyword(marketId, condition, date)
        }

    fun findActiveProductList(
        marketId: Long,
        condition: ProductListSearchCondition,
        date: LocalDate,
    ): CursorSliceResult<ProductListItem> {
        // 키셋: 커서 productId로 그 행의 정렬 기준값을 되짚는다(정렬이 id가 아닌 경우만 필요).
        // anchor 조회는 VIEW_COUNT/CATEGORY 분기 안에서만 수행한다(LATEST는 정렬이 id 자체라 되짚기 불필요).
        val rows =
            when (condition.sort) {
                ProductSortType.LATEST ->
                    productRepository.findActiveByLatest(marketId, condition, date, condition.size + 1)
                ProductSortType.VIEW_COUNT ->
                    withCursorAnchor(marketId, condition) { anchor ->
                        productRepository.findActiveByViewCount(marketId, condition, date, anchor?.viewCount, condition.size + 1)
                    }
                ProductSortType.CATEGORY ->
                    withCursorAnchor(marketId, condition) { anchor ->
                        productRepository.findActiveByCategoryOrder(
                            marketId,
                            condition,
                            date,
                            anchor?.categoryOrder,
                            condition.size + 1,
                        )
                    }
            }
        return rows.toCursorSlice(condition.size) { it.product.id }
    }

    // 커서 상품이 그 사이 삭제돼 anchor를 못 찾으면 빈 결과(마지막 페이지)로 처리한다 — 페이지 경계에서 잘못된 중복 노출 방지.
    private fun withCursorAnchor(
        marketId: Long,
        condition: ProductListSearchCondition,
        query: (ProductListCursorAnchor?) -> List<ProductListItem>,
    ): List<ProductListItem> {
        if (condition.cursor == null) return query(null)
        val anchor = productRepository.findListCursorAnchor(condition.cursor, marketId) ?: return emptyList()
        return query(anchor)
    }
}
