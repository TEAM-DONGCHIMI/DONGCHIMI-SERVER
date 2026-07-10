package kr.dongchimi.core.market

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kr.dongchimi.core.product.DealType
import kr.dongchimi.core.product.DiscountPeriod
import kr.dongchimi.core.product.PeriodicProductSearchCondition
import kr.dongchimi.core.product.Price
import kr.dongchimi.core.product.Product
import kr.dongchimi.core.product.ProductCategory
import kr.dongchimi.core.product.ProductListCursorAnchor
import kr.dongchimi.core.product.ProductListItem
import kr.dongchimi.core.product.ProductListSearchCondition
import kr.dongchimi.core.product.ProductRepository
import kr.dongchimi.core.product.ProductSortType
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

class ProductFinderTest :
    FunSpec({
        val marketId = 10L

        fun condition(
            sort: ProductSortType,
            cursor: Long? = null,
            size: Int = 12,
        ): ProductListSearchCondition =
            ProductListSearchCondition(dealType = DealType.PERIODIC, category = null, sort = sort, cursor = cursor, size = size)

        test("LATEST 정렬: cursor가 있어도 anchor를 조회하지 않는다") {
            val repository = StubProductRepository(latestResult = listOf(listItem(id = 1L)))
            val finder = ProductFinder(repository)

            finder.findActiveProductList(marketId, condition(ProductSortType.LATEST, cursor = 5L), LocalDate.now())

            repository.latestCalled shouldBe true
            repository.anchorRequestedCursor shouldBe null
        }

        test("VIEW_COUNT 정렬: cursor가 있으면 anchor를 되짚어 cursorViewCount로 전달한다") {
            val anchor = ProductListCursorAnchor(categoryOrder = 1, viewCount = 42)
            val repository = StubProductRepository(anchor = anchor, viewCountResult = listOf(listItem(id = 1L)))
            val finder = ProductFinder(repository)

            finder.findActiveProductList(marketId, condition(ProductSortType.VIEW_COUNT, cursor = 5L), LocalDate.now())

            repository.anchorRequestedCursor shouldBe 5L
            repository.viewCountCalledWithCursorViewCount shouldBe 42
        }

        test("CATEGORY 정렬: cursor가 있으면 anchor를 되짚어 cursorCategoryOrder로 전달한다") {
            val anchor = ProductListCursorAnchor(categoryOrder = 3, viewCount = 0)
            val repository = StubProductRepository(anchor = anchor, categoryOrderResult = listOf(listItem(id = 1L)))
            val finder = ProductFinder(repository)

            finder.findActiveProductList(marketId, condition(ProductSortType.CATEGORY, cursor = 5L), LocalDate.now())

            repository.anchorRequestedCursor shouldBe 5L
            repository.categoryOrderCalledWithCursorCategoryOrder shouldBe 3
        }

        test("VIEW_COUNT/CATEGORY 정렬: 첫 페이지(cursor 없음)면 anchor를 조회하지 않는다") {
            val repository = StubProductRepository(viewCountResult = listOf(listItem(id = 1L)))
            val finder = ProductFinder(repository)

            finder.findActiveProductList(marketId, condition(ProductSortType.VIEW_COUNT, cursor = null), LocalDate.now())

            repository.anchorRequestedCursor shouldBe null
            repository.viewCountCalledWithCursorViewCount shouldBe null
        }

        test("커서 상품이 삭제되어 anchor가 없으면 빈 슬라이스를 반환한다") {
            val repository = StubProductRepository(anchor = null, viewCountResult = listOf(listItem(id = 1L)))
            val finder = ProductFinder(repository)

            val result = finder.findActiveProductList(marketId, condition(ProductSortType.VIEW_COUNT, cursor = 5L), LocalDate.now())

            result.content shouldBe emptyList()
            result.hasNext shouldBe false
            result.nextCursor shouldBe null
            repository.viewCountCalled shouldBe false
        }
    })

private fun listItem(id: Long): ProductListItem =
    ProductListItem(
        product =
            Product(
                id = id,
                marketId = 10L,
                name = "삼겹살 500g",
                dealType = DealType.PERIODIC,
                thumbnailUrl = null,
                price = Price(BigDecimal("15000"), BigDecimal("12000")),
                category = ProductCategory.MEAT_EGG,
                promotionalPhrase = null,
                discountPeriod = DiscountPeriod(LocalDate.of(2025, 8, 1), LocalDate.of(2025, 8, 16)),
            ),
        viewCount = 0,
        createdAt = LocalDateTime.of(2025, 1, 1, 0, 0),
    )

private class StubProductRepository(
    private val anchor: ProductListCursorAnchor? = null,
    private val latestResult: List<ProductListItem> = emptyList(),
    private val viewCountResult: List<ProductListItem> = emptyList(),
    private val categoryOrderResult: List<ProductListItem> = emptyList(),
) : ProductRepository {
    var latestCalled = false
        private set
    var viewCountCalled = false
        private set
    var viewCountCalledWithCursorViewCount: Int? = null
        private set
    var categoryOrderCalled = false
        private set
    var categoryOrderCalledWithCursorCategoryOrder: Int? = null
        private set
    var anchorRequestedCursor: Long? = null
        private set

    override fun findActiveByLatest(
        marketId: Long,
        condition: ProductListSearchCondition,
        date: LocalDate,
        limit: Int,
    ): List<ProductListItem> {
        latestCalled = true
        return latestResult
    }

    override fun findActiveByViewCount(
        marketId: Long,
        condition: ProductListSearchCondition,
        date: LocalDate,
        cursorViewCount: Int?,
        limit: Int,
    ): List<ProductListItem> {
        viewCountCalled = true
        viewCountCalledWithCursorViewCount = cursorViewCount
        return viewCountResult
    }

    override fun findActiveByCategoryOrder(
        marketId: Long,
        condition: ProductListSearchCondition,
        date: LocalDate,
        cursorCategoryOrder: Int?,
        limit: Int,
    ): List<ProductListItem> {
        categoryOrderCalled = true
        categoryOrderCalledWithCursorCategoryOrder = cursorCategoryOrder
        return categoryOrderResult
    }

    override fun findListCursorAnchor(
        cursor: Long,
        marketId: Long,
    ): ProductListCursorAnchor? {
        anchorRequestedCursor = cursor
        return anchor
    }

    override fun findById(id: Long): Product? = throw UnsupportedOperationException()

    override fun findAllByIds(ids: List<Long>): List<Product> = throw UnsupportedOperationException()

    override fun findAllByMarketIdAndDealType(
        marketId: Long,
        dealType: DealType,
    ): List<Product> = throw UnsupportedOperationException()

    override fun save(product: Product): Product = throw UnsupportedOperationException()

    override fun saveAll(products: List<Product>): List<Product> = throw UnsupportedOperationException()

    override fun update(product: Product): Unit = throw UnsupportedOperationException()

    override fun softDeleteByIds(ids: List<Long>): Unit = throw UnsupportedOperationException()

    override fun softDeleteByMarketIdAndDealType(
        marketId: Long,
        dealType: DealType,
    ): Unit = throw UnsupportedOperationException()

    override fun countProductsInMarket(
        productIds: List<Long>,
        marketId: Long,
    ): Int = throw UnsupportedOperationException()

    override fun updateDiscountPeriod(
        productIds: List<Long>,
        discountPeriod: DiscountPeriod,
    ): Unit = throw UnsupportedOperationException()

    override fun findActiveByMarketIdAndDealType(
        marketId: Long,
        dealType: DealType,
        date: LocalDate,
        limit: Int,
    ): List<Product> = throw UnsupportedOperationException()

    override fun findAllActiveByMarketIdAndDealType(
        marketId: Long,
        dealType: DealType,
        date: LocalDate,
    ): List<Product> = throw UnsupportedOperationException()

    override fun countActiveByMarketIdAndDealType(
        marketId: Long,
        dealType: DealType,
        date: LocalDate,
    ): Int = throw UnsupportedOperationException()

    override fun countRegisteredOn(
        marketId: Long,
        date: LocalDate,
    ): Int = throw UnsupportedOperationException()

    override fun findPopularActive(
        marketId: Long,
        date: LocalDate,
        limit: Int,
    ): List<Product> = throw UnsupportedOperationException()

    override fun findLatestActiveByMarketIds(
        marketIds: List<Long>,
        date: LocalDate,
        limitPerMarket: Int,
    ): List<Product> = throw UnsupportedOperationException()

    override fun countActiveByMarketIds(
        marketIds: List<Long>,
        date: LocalDate,
    ): Map<Long, Int> = throw UnsupportedOperationException()

    override fun findActiveByMarketIdAndDealTypeAndCategory(
        marketId: Long,
        dealType: DealType,
        condition: PeriodicProductSearchCondition,
        date: LocalDate,
        limit: Int,
    ): List<Product> = throw UnsupportedOperationException()
}
