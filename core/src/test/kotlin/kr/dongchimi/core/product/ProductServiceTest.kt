package kr.dongchimi.core.product

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kr.dongchimi.core.common.exception.CoreException
import kr.dongchimi.core.market.BusinessHours
import kr.dongchimi.core.market.LocationPoint
import kr.dongchimi.core.market.Market
import kr.dongchimi.core.market.MarketErrorCode
import kr.dongchimi.core.market.MarketInfo
import kr.dongchimi.core.market.MarketPhoneNumber
import kr.dongchimi.core.market.MarketRepository
import kr.dongchimi.core.market.MarketValidator
import kr.dongchimi.core.market.ProductFinder
import java.math.BigDecimal
import java.time.LocalDate

class ProductServiceTest :
    FunSpec({
        val ownerId = 1L
        val marketId = 10L

        fun newService(
            markets: FakeMarketRepository = FakeMarketRepository(),
            products: FakeProductRepository = FakeProductRepository(),
        ): Triple<ProductService, FakeMarketRepository, FakeProductRepository> {
            val service =
                ProductService(
                    marketValidator = MarketValidator(markets),
                    productReader = ProductReader(products),
                    productValidator = ProductValidator(products),
                    productRemover = ProductRemover(products),
                    productUpdater = ProductUpdater(products),
                    productFinder = ProductFinder(products),
                )
            return Triple(service, markets, products)
        }

        test("상세 조회: 마트가 없으면 MARKET_NOT_FOUND") {
            val (service, _, _) = newService()

            val exception = shouldThrow<CoreException> { service.getProduct(ownerId, marketId, 1L) }

            exception.errorCode shouldBe MarketErrorCode.MARKET_NOT_FOUND
        }

        test("상세 조회: 다른 점주 소유 마트면 MARKET_ACCESS_DENIED") {
            val markets = FakeMarketRepository().apply { put(marketId, ownerId = 2L) }
            val (service, _, _) = newService(markets)

            val exception = shouldThrow<CoreException> { service.getProduct(ownerId, marketId, 1L) }

            exception.errorCode shouldBe MarketErrorCode.MARKET_ACCESS_DENIED
        }

        test("상세 조회: 상품이 해당 마트에 없으면 PRODUCT_NOT_FOUND") {
            val markets = FakeMarketRepository().apply { put(marketId, ownerId) }
            val products = FakeProductRepository().apply { put(sampleProduct(id = 1L, marketId = 999L)) }
            val (service, _, _) = newService(markets, products)

            val exception = shouldThrow<CoreException> { service.getProduct(ownerId, marketId, 1L) }

            exception.errorCode shouldBe ProductErrorCode.PRODUCT_NOT_FOUND
        }

        test("상세 조회: 정상이면 상품을 반환한다") {
            val markets = FakeMarketRepository().apply { put(marketId, ownerId) }
            val products = FakeProductRepository().apply { put(sampleProduct(id = 1L, marketId = marketId)) }
            val (service, _, _) = newService(markets, products)

            val product = service.getProduct(ownerId, marketId, 1L)

            product.id shouldBe 1L
            product.marketId shouldBe marketId
        }

        test("삭제: 상품이 해당 마트에 없으면 PRODUCT_NOT_FOUND") {
            val markets = FakeMarketRepository().apply { put(marketId, ownerId) }
            val products = FakeProductRepository().apply { put(sampleProduct(id = 1L, marketId = 999L)) }
            val (service, _, _) = newService(markets, products)

            val exception = shouldThrow<CoreException> { service.delete(ownerId, marketId, 1L) }

            exception.errorCode shouldBe ProductErrorCode.PRODUCT_NOT_FOUND
        }

        test("삭제: 정상이면 soft delete 된다") {
            val markets = FakeMarketRepository().apply { put(marketId, ownerId) }
            val products = FakeProductRepository().apply { put(sampleProduct(id = 1L, marketId = marketId)) }
            val (service, _, _) = newService(markets, products)

            service.delete(ownerId, marketId, 1L)

            products.findById(1L) shouldBe null
        }

        test("일괄 삭제: 일부가 해당 마트에 없으면 PRODUCT_NOT_FOUND") {
            val markets = FakeMarketRepository().apply { put(marketId, ownerId) }
            val products =
                FakeProductRepository().apply {
                    put(sampleProduct(id = 1L, marketId = marketId))
                    put(sampleProduct(id = 3L, marketId = 999L))
                }
            val (service, _, _) = newService(markets, products)

            val exception = shouldThrow<CoreException> { service.deleteAll(ownerId, marketId, listOf(1L, 2L, 3L)) }

            exception.errorCode shouldBe ProductErrorCode.PRODUCT_NOT_FOUND
        }

        test("일괄 삭제: 모두 해당 마트 소속이면 전부 soft delete 된다") {
            val markets = FakeMarketRepository().apply { put(marketId, ownerId) }
            val products =
                FakeProductRepository().apply {
                    put(sampleProduct(id = 1L, marketId = marketId))
                    put(sampleProduct(id = 2L, marketId = marketId))
                }
            val (service, _, _) = newService(markets, products)

            service.deleteAll(ownerId, marketId, listOf(1L, 2L))

            products.findById(1L) shouldBe null
            products.findById(2L) shouldBe null
        }

        test("일괄 삭제: 할인 기간이 끝나지 않은 상품이 있으면 DISCOUNT_NOT_ENDED") {
            val markets = FakeMarketRepository().apply { put(marketId, ownerId) }
            val products =
                FakeProductRepository().apply {
                    put(sampleProduct(id = 1L, marketId = marketId))
                    put(sampleProduct(id = 2L, marketId = marketId, discountPeriod = ongoingDiscount()))
                }
            val (service, _, _) = newService(markets, products)

            val exception = shouldThrow<CoreException> { service.deleteAll(ownerId, marketId, listOf(1L, 2L)) }

            exception.errorCode shouldBe ProductErrorCode.DISCOUNT_NOT_ENDED
            products.findById(1L)!!.id shouldBe 1L
        }

        test("기간 일괄 수정: 일부가 해당 마트에 없으면 PRODUCT_NOT_FOUND") {
            val markets = FakeMarketRepository().apply { put(marketId, ownerId) }
            val products = FakeProductRepository().apply { put(sampleProduct(id = 1L, marketId = marketId)) }
            val (service, _, _) = newService(markets, products)

            val period = DiscountPeriod(LocalDate.of(2025, 8, 1), LocalDate.of(2025, 8, 16))
            val exception =
                shouldThrow<CoreException> { service.updateDiscountPeriod(ownerId, marketId, listOf(1L, 2L), period) }

            exception.errorCode shouldBe ProductErrorCode.PRODUCT_NOT_FOUND
        }

        test("기간 일괄 수정: 정상이면 기간이 반영된다") {
            val markets = FakeMarketRepository().apply { put(marketId, ownerId) }
            val products = FakeProductRepository().apply { put(sampleProduct(id = 1L, marketId = marketId)) }
            val (service, _, _) = newService(markets, products)

            val period = DiscountPeriod(LocalDate.of(2025, 9, 1), LocalDate.of(2025, 9, 30))
            service.updateDiscountPeriod(ownerId, marketId, listOf(1L), period)

            products.findById(1L)!!.discountPeriod shouldBe period
        }

        test("초기화: 해당 dealType 상품만 삭제되고 다른 dealType은 유지된다") {
            val markets = FakeMarketRepository().apply { put(marketId, ownerId) }
            val products =
                FakeProductRepository().apply {
                    put(sampleProduct(id = 1L, marketId = marketId, dealType = DealType.DAILY))
                    put(sampleProduct(id = 2L, marketId = marketId, dealType = DealType.PERIODIC))
                }
            val (service, _, _) = newService(markets, products)

            service.reset(ownerId, marketId, DealType.DAILY)

            products.findById(1L) shouldBe null
            products.findById(2L)!!.id shouldBe 2L
        }

        test("초기화: 할인 기간이 끝나지 않은 상품이 있으면 DISCOUNT_NOT_ENDED") {
            val markets = FakeMarketRepository().apply { put(marketId, ownerId) }
            val products =
                FakeProductRepository().apply {
                    put(sampleProduct(id = 1L, marketId = marketId, dealType = DealType.DAILY))
                    put(
                        sampleProduct(
                            id = 2L,
                            marketId = marketId,
                            dealType = DealType.DAILY,
                            discountPeriod = ongoingDiscount(),
                        ),
                    )
                }
            val (service, _, _) = newService(markets, products)

            val exception = shouldThrow<CoreException> { service.reset(ownerId, marketId, DealType.DAILY) }

            exception.errorCode shouldBe ProductErrorCode.DISCOUNT_NOT_ENDED
            products.findById(1L)!!.id shouldBe 1L
        }

        test("오늘의 특가: 마트가 없으면 MARKET_NOT_FOUND") {
            val (service, _, _) = newService()

            val exception =
                shouldThrow<CoreException> { service.getAllActiveProducts(marketId, DealType.DAILY, LocalDate.now()) }

            exception.errorCode shouldBe MarketErrorCode.MARKET_NOT_FOUND
        }

        test("오늘의 특가: DAILY이면서 오늘 할인 진행 중인 상품만 반환한다") {
            val markets = FakeMarketRepository().apply { put(marketId, ownerId) }
            val products =
                FakeProductRepository().apply {
                    put(sampleProduct(id = 1L, marketId = marketId, dealType = DealType.DAILY, discountPeriod = ongoingDiscount()))
                    put(sampleProduct(id = 2L, marketId = marketId, dealType = DealType.PERIODIC, discountPeriod = ongoingDiscount()))
                    put(sampleProduct(id = 3L, marketId = marketId, dealType = DealType.DAILY))
                }
            val (service, _, _) = newService(markets, products)

            val result = service.getAllActiveProducts(marketId, DealType.DAILY, LocalDate.now())

            result.map { it.id } shouldBe listOf(1L)
        }
    })

private fun ongoingDiscount(): DiscountPeriod = DiscountPeriod(LocalDate.now().minusDays(1), LocalDate.now().plusDays(30))

private fun sampleProduct(
    id: Long,
    marketId: Long,
    dealType: DealType = DealType.PERIODIC,
    discountPeriod: DiscountPeriod = DiscountPeriod(LocalDate.of(2025, 8, 1), LocalDate.of(2025, 8, 16)),
): Product =
    Product(
        id = id,
        marketId = marketId,
        name = "삼겹살 500g",
        dealType = dealType,
        thumbnailUrl = "https://cdn.example.com/products/$id.png",
        price = Price(BigDecimal("15000"), BigDecimal("12000")),
        category = ProductCategory.MEAT_EGG,
        promotionalPhrase = null,
        discountPeriod = discountPeriod,
    )

private class FakeMarketRepository : MarketRepository {
    private val store = mutableMapOf<Long, Market>()

    fun put(
        id: Long,
        ownerId: Long,
    ) {
        store[id] =
            Market(
                id = id,
                ownerId = ownerId,
                info = MarketInfo(name = "동치미 마트", address = "서울특별시 성북구", thumbnailUrl = null),
                location = LocationPoint(longitude = 127.0, latitude = 37.0),
                businessHours = BusinessHours(emptyList()),
                phoneNumber = MarketPhoneNumber("02-000-0000", null, 1, "010-0000-0000"),
                brn = null,
            )
    }

    override fun findById(id: Long): Market? = store[id]

    override fun findByOwnerId(ownerId: Long): Market? = store.values.firstOrNull { it.ownerId == ownerId }

    override fun save(market: Market): Market = market

    override fun existsByOwnerIdAndName(
        ownerId: Long,
        name: String,
    ): Boolean = false

    override fun existsByOwnerIdAndNameAndIdNot(
        ownerId: Long,
        name: String,
        id: Long,
    ): Boolean = false

    override fun existsByIdAndOwnerId(
        marketId: Long,
        ownerId: Long,
    ): Boolean = store[marketId]?.ownerId == ownerId

    override fun existsById(id: Long): Boolean = store.containsKey(id)
}

private class FakeProductRepository : ProductRepository {
    private val store = mutableMapOf<Long, Product>()

    fun put(product: Product) {
        store[product.id] = product
    }

    override fun findById(id: Long): Product? = store[id]

    override fun saveAll(products: List<Product>): List<Product> {
        products.forEach { store[it.id] = it }
        return products
    }

    override fun findAllByIds(ids: List<Long>): List<Product> = ids.mapNotNull { store[it] }

    override fun findAllByMarketIdAndDealType(
        marketId: Long,
        dealType: DealType,
    ): List<Product> = store.values.filter { it.marketId == marketId && it.dealType == dealType }

    override fun save(product: Product): Product {
        store[product.id] = product
        return product
    }

    override fun softDeleteByIds(ids: List<Long>) {
        ids.forEach { store.remove(it) }
    }

    override fun softDeleteByMarketIdAndDealType(
        marketId: Long,
        dealType: DealType,
    ) {
        store.values.filter { it.marketId == marketId && it.dealType == dealType }.forEach { store.remove(it.id) }
    }

    override fun countProductsInMarket(
        productIds: List<Long>,
        marketId: Long,
    ): Int = productIds.count { store[it]?.marketId == marketId }

    override fun updateDiscountPeriod(
        productIds: List<Long>,
        discountPeriod: DiscountPeriod,
    ) {
        productIds.forEach { id -> store[id]?.let { store[id] = it.copy(discountPeriod = discountPeriod) } }
    }

    override fun findActiveByMarketIdAndDealType(
        marketId: Long,
        dealType: DealType,
        date: LocalDate,
        limit: Int,
    ): List<Product> =
        store.values
            .filter { it.marketId == marketId && it.dealType == dealType && isActiveOn(it, date) }
            .take(limit)

    override fun findAllActiveByMarketIdAndDealType(
        marketId: Long,
        dealType: DealType,
        date: LocalDate,
    ): List<Product> = store.values.filter { it.marketId == marketId && it.dealType == dealType && isActiveOn(it, date) }

    override fun countActiveByMarketIdAndDealType(
        marketId: Long,
        dealType: DealType,
        date: LocalDate,
    ): Int = store.values.count { it.marketId == marketId && it.dealType == dealType && isActiveOn(it, date) }

    override fun countRegisteredOn(
        marketId: Long,
        date: LocalDate,
    ): Int = store.values.count { it.marketId == marketId }

    override fun findPopularActive(
        marketId: Long,
        date: LocalDate,
        limit: Int,
    ): List<Product> =
        store.values
            .filter { it.marketId == marketId && isActiveOn(it, date) }
            .take(limit)

    private fun isActiveOn(
        product: Product,
        date: LocalDate,
    ): Boolean = product.discountPeriod.discountStartDate <= date && product.discountPeriod.discountEndDate >= date
}
