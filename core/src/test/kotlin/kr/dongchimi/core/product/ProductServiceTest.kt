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
import kr.dongchimi.core.market.NearbyMarket
import kr.dongchimi.core.market.NearbyMarketSearchCondition
import kr.dongchimi.core.market.ProductFinder
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

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
                    productAppender = ProductAppender(products),
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

        test("상세 조회(user): 상품이 해당 마트에 없으면 PRODUCT_NOT_FOUND") {
            val products = FakeProductRepository().apply { put(sampleProduct(id = 1L, marketId = 999L)) }
            val (service, _, _) = newService(products = products)

            val exception = shouldThrow<CoreException> { service.getDetail(marketId, 1L) }

            exception.errorCode shouldBe ProductErrorCode.PRODUCT_NOT_FOUND
        }

        test("상세 조회(user): 상품이 없으면 PRODUCT_NOT_FOUND") {
            val (service, _, _) = newService()

            val exception = shouldThrow<CoreException> { service.getDetail(marketId, 1L) }

            exception.errorCode shouldBe ProductErrorCode.PRODUCT_NOT_FOUND
        }

        test("상세 조회(user): 정상이면 상품을 반환한다") {
            val products = FakeProductRepository().apply { put(sampleProduct(id = 1L, marketId = marketId)) }
            val (service, _, _) = newService(products = products)

            val product = service.getDetail(marketId, 1L)

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

        test("오늘의 특가 등록: 마트가 없으면 MARKET_NOT_FOUND") {
            val (service, _, _) = newService()

            val exception =
                shouldThrow<CoreException> {
                    service.registerDailyProduct(ownerId, marketId, registerCommand(), LocalDate.now())
                }

            exception.errorCode shouldBe MarketErrorCode.MARKET_NOT_FOUND
        }

        test("오늘의 특가 등록: 다른 점주 소유 마트면 MARKET_ACCESS_DENIED") {
            val markets = FakeMarketRepository().apply { put(marketId, ownerId = 2L) }
            val (service, _, _) = newService(markets)

            val exception =
                shouldThrow<CoreException> {
                    service.registerDailyProduct(ownerId, marketId, registerCommand(), LocalDate.now())
                }

            exception.errorCode shouldBe MarketErrorCode.MARKET_ACCESS_DENIED
        }

        test("오늘의 특가 등록: 기간이 오늘을 포함하지 않으면 INVALID_DISCOUNT_PERIOD") {
            val markets = FakeMarketRepository().apply { put(marketId, ownerId) }
            val (service, _, products) = newService(markets)
            val today = LocalDate.of(2026, 7, 10)
            val endedYesterday = DiscountPeriod(today.minusDays(3), today.minusDays(1))

            val exception =
                shouldThrow<CoreException> {
                    service.registerDailyProduct(ownerId, marketId, registerCommand(discountPeriod = endedYesterday), today)
                }

            exception.errorCode shouldBe ProductErrorCode.INVALID_DISCOUNT_PERIOD
            products.findById(0L) shouldBe null
        }

        test("오늘의 특가 등록: 정상이면 DAILY 상품으로 저장된다") {
            val markets = FakeMarketRepository().apply { put(marketId, ownerId) }
            val (service, _, products) = newService(markets)
            val today = LocalDate.of(2026, 7, 10)
            val period = DiscountPeriod(today.minusDays(1), today.plusDays(1))

            service.registerDailyProduct(ownerId, marketId, registerCommand(discountPeriod = period), today)

            val saved = products.findById(0L)!!
            saved.dealType shouldBe DealType.DAILY
            saved.marketId shouldBe marketId
            saved.thumbnailUrl shouldBe "https://cdn.example.com/products/new.png"
        }

        test("오늘의 특가 등록: 썸네일이 없으면 null로 저장된다") {
            val markets = FakeMarketRepository().apply { put(marketId, ownerId) }
            val (service, _, products) = newService(markets)
            val today = LocalDate.of(2026, 7, 10)
            val period = DiscountPeriod(today, today)

            service.registerDailyProduct(ownerId, marketId, registerCommand(thumbnailUrl = null, discountPeriod = period), today)

            products.findById(0L)!!.thumbnailUrl shouldBe null
        }

        test("상품 수정: 상품이 해당 마트에 없으면 PRODUCT_NOT_FOUND") {
            val markets = FakeMarketRepository().apply { put(marketId, ownerId) }
            val products = FakeProductRepository().apply { put(sampleProduct(id = 1L, marketId = 999L)) }
            val (service, _, _) = newService(markets, products)

            val exception =
                shouldThrow<CoreException> {
                    service.updateProduct(ownerId, marketId, 1L, updateCommand(dealType = DealType.PERIODIC), LocalDate.now())
                }

            exception.errorCode shouldBe ProductErrorCode.PRODUCT_NOT_FOUND
        }

        test("상품 수정: 요청 type이 상품의 dealType과 다르면 TYPE_MISMATCH") {
            val markets = FakeMarketRepository().apply { put(marketId, ownerId) }
            val products = FakeProductRepository().apply { put(sampleProduct(id = 1L, marketId = marketId, dealType = DealType.PERIODIC)) }
            val (service, _, _) = newService(markets, products)

            val exception =
                shouldThrow<CoreException> {
                    service.updateProduct(ownerId, marketId, 1L, updateCommand(dealType = DealType.DAILY), LocalDate.now())
                }

            exception.errorCode shouldBe ProductErrorCode.TYPE_MISMATCH
        }

        test("상품 수정: DAILY인데 기간이 오늘을 포함하지 않으면 INVALID_DISCOUNT_PERIOD") {
            val markets = FakeMarketRepository().apply { put(marketId, ownerId) }
            val products = FakeProductRepository().apply { put(sampleProduct(id = 1L, marketId = marketId, dealType = DealType.DAILY)) }
            val (service, _, _) = newService(markets, products)
            val today = LocalDate.of(2026, 7, 10)
            val past = DiscountPeriod(today.minusDays(3), today.minusDays(1))

            val exception =
                shouldThrow<CoreException> {
                    service.updateProduct(ownerId, marketId, 1L, updateCommand(dealType = DealType.DAILY, discountPeriod = past), today)
                }

            exception.errorCode shouldBe ProductErrorCode.INVALID_DISCOUNT_PERIOD
        }

        test("상품 수정: PERIODIC은 기간이 오늘을 포함하지 않아도 통과한다") {
            val markets = FakeMarketRepository().apply { put(marketId, ownerId) }
            val products = FakeProductRepository().apply { put(sampleProduct(id = 1L, marketId = marketId, dealType = DealType.PERIODIC)) }
            val (service, _, _) = newService(markets, products)
            val today = LocalDate.of(2026, 7, 10)
            val future = DiscountPeriod(today.plusDays(1), today.plusDays(5))

            service.updateProduct(ownerId, marketId, 1L, updateCommand(dealType = DealType.PERIODIC, discountPeriod = future), today)

            products.findById(1L)!!.discountPeriod shouldBe future
        }

        test("상품 수정: 정상이면 필드가 반영되고 id·marketId·dealType은 유지된다") {
            val markets = FakeMarketRepository().apply { put(marketId, ownerId) }
            val products = FakeProductRepository().apply { put(sampleProduct(id = 1L, marketId = marketId, dealType = DealType.PERIODIC)) }
            val (service, _, _) = newService(markets, products)

            service.updateProduct(ownerId, marketId, 1L, updateCommand(dealType = DealType.PERIODIC, name = "수정된 상품"), LocalDate.now())

            val updated = products.findById(1L)!!
            updated.id shouldBe 1L
            updated.marketId shouldBe marketId
            updated.dealType shouldBe DealType.PERIODIC
            updated.name shouldBe "수정된 상품"
        }
        test("행사 할인 상품: PERIODIC이면서 오늘 할인 진행 중인 상품만 반환한다") {
            val markets = FakeMarketRepository().apply { put(marketId, ownerId) }
            val products =
                FakeProductRepository().apply {
                    put(sampleProduct(id = 1L, marketId = marketId, dealType = DealType.PERIODIC, discountPeriod = ongoingDiscount()))
                    put(sampleProduct(id = 2L, marketId = marketId, dealType = DealType.DAILY, discountPeriod = ongoingDiscount()))
                    put(sampleProduct(id = 3L, marketId = marketId, dealType = DealType.PERIODIC))
                }
            val (service, _, _) = newService(markets, products)

            val result = service.getActiveProductsByCategory(marketId, DealType.PERIODIC, periodicCondition(), LocalDate.now())

            result.content.map { it.id } shouldBe listOf(1L)
        }

        test("행사 할인 상품: category가 있으면 해당 카테고리만 반환한다") {
            val markets = FakeMarketRepository().apply { put(marketId, ownerId) }
            val products =
                FakeProductRepository().apply {
                    put(
                        sampleProduct(
                            id = 1L,
                            marketId = marketId,
                            discountPeriod = ongoingDiscount(),
                            category = ProductCategory.MEAT_EGG,
                        ),
                    )
                    put(
                        sampleProduct(
                            id = 2L,
                            marketId = marketId,
                            discountPeriod = ongoingDiscount(),
                            category = ProductCategory.SEAFOOD,
                        ),
                    )
                }
            val (service, _, _) = newService(markets, products)

            val result =
                service.getActiveProductsByCategory(
                    marketId,
                    DealType.PERIODIC,
                    periodicCondition(category = ProductCategory.SEAFOOD),
                    LocalDate.now(),
                )

            result.content.map { it.id } shouldBe listOf(2L)
        }

        test("행사 할인 상품: size보다 많이 조회되면 hasNext가 true이고 마지막 상품 id가 nextCursor다") {
            val markets = FakeMarketRepository().apply { put(marketId, ownerId) }
            val products =
                FakeProductRepository().apply {
                    put(sampleProduct(id = 1L, marketId = marketId, discountPeriod = ongoingDiscount()))
                    put(sampleProduct(id = 2L, marketId = marketId, discountPeriod = ongoingDiscount()))
                    put(sampleProduct(id = 3L, marketId = marketId, discountPeriod = ongoingDiscount()))
                }
            val (service, _, _) = newService(markets, products)

            val result = service.getActiveProductsByCategory(marketId, DealType.PERIODIC, periodicCondition(size = 2), LocalDate.now())

            result.content.map { it.id } shouldBe listOf(3L, 2L)
            result.hasNext shouldBe true
            result.nextCursor shouldBe 2L
        }

        test("행사 할인 상품: size 이하로 조회되면 hasNext가 false이고 nextCursor는 null이다") {
            val markets = FakeMarketRepository().apply { put(marketId, ownerId) }
            val products =
                FakeProductRepository().apply {
                    put(sampleProduct(id = 1L, marketId = marketId, discountPeriod = ongoingDiscount()))
                }
            val (service, _, _) = newService(markets, products)

            val result = service.getActiveProductsByCategory(marketId, DealType.PERIODIC, periodicCondition(size = 2), LocalDate.now())

            result.hasNext shouldBe false
            result.nextCursor shouldBe null
        }

        test("상품 목록 조회(점주): 마트가 없으면 MARKET_NOT_FOUND") {
            val (service, _, _) = newService()

            val exception =
                shouldThrow<CoreException> {
                    service.getOwnerProducts(ownerId, marketId, ownerProductListCondition(), LocalDate.now())
                }

            exception.errorCode shouldBe MarketErrorCode.MARKET_NOT_FOUND
        }

        test("상품 목록 조회(점주): 다른 점주 소유 마트면 MARKET_ACCESS_DENIED") {
            val markets = FakeMarketRepository().apply { put(marketId, ownerId = 2L) }
            val (service, _, _) = newService(markets)

            val exception =
                shouldThrow<CoreException> {
                    service.getOwnerProducts(ownerId, marketId, ownerProductListCondition(), LocalDate.now())
                }

            exception.errorCode shouldBe MarketErrorCode.MARKET_ACCESS_DENIED
        }

        test("상품 목록 조회(점주): size보다 많이 조회되면 hasNext가 true이고 마지막 상품 id가 nextCursor다") {
            val markets = FakeMarketRepository().apply { put(marketId, ownerId) }
            val products =
                FakeProductRepository().apply {
                    put(sampleProduct(id = 1L, marketId = marketId, discountPeriod = ongoingDiscount()))
                    put(sampleProduct(id = 2L, marketId = marketId, discountPeriod = ongoingDiscount()))
                    put(sampleProduct(id = 3L, marketId = marketId, discountPeriod = ongoingDiscount()))
                }
            val (service, _, _) = newService(markets, products)

            val result = service.getOwnerProducts(ownerId, marketId, ownerProductListCondition(size = 2), LocalDate.now())

            result.content.map { it.product.id } shouldBe listOf(3L, 2L)
            result.hasNext shouldBe true
            result.nextCursor shouldBe 2L
        }

        test("상품 목록 조회(점주): size 이하로 조회되면 hasNext가 false이고 nextCursor는 null이다") {
            val markets = FakeMarketRepository().apply { put(marketId, ownerId) }
            val products =
                FakeProductRepository().apply {
                    put(sampleProduct(id = 1L, marketId = marketId, discountPeriod = ongoingDiscount()))
                }
            val (service, _, _) = newService(markets, products)

            val result = service.getOwnerProducts(ownerId, marketId, ownerProductListCondition(size = 2), LocalDate.now())

            result.hasNext shouldBe false
            result.nextCursor shouldBe null
        }
    })

private fun periodicCondition(
    category: ProductCategory? = null,
    cursor: Long? = null,
    size: Int = 12,
): PeriodicProductSearchCondition = PeriodicProductSearchCondition(category = category, cursor = cursor, size = size)

private fun ownerProductListCondition(
    dealType: DealType = DealType.PERIODIC,
    category: ProductCategory? = null,
    sort: ProductSortType = ProductSortType.LATEST,
    cursor: Long? = null,
    size: Int = 12,
): ProductListSearchCondition =
    ProductListSearchCondition(dealType = dealType, category = category, sort = sort, cursor = cursor, size = size)

private fun updateCommand(
    dealType: DealType,
    name: String = "수정된 상품",
    discountPeriod: DiscountPeriod = DiscountPeriod(LocalDate.now(), LocalDate.now()),
): ProductUpdateCommand =
    ProductUpdateCommand(
        dealType = dealType,
        name = name,
        thumbnailUrl = "https://cdn.example.com/products/edit.png",
        price = Price(BigDecimal("15000"), BigDecimal("12000")),
        category = ProductCategory.MEAT_EGG,
        promotionalPhrase = null,
        discountPeriod = discountPeriod,
    )

private fun registerCommand(
    thumbnailUrl: String? = "https://cdn.example.com/products/new.png",
    discountPeriod: DiscountPeriod = DiscountPeriod(LocalDate.now(), LocalDate.now()),
): DailyProductRegisterCommand =
    DailyProductRegisterCommand(
        name = "토마토",
        thumbnailUrl = thumbnailUrl,
        price = Price(BigDecimal("5000"), BigDecimal("4500")),
        category = ProductCategory.VEGETABLE_FRUIT,
        promotionalPhrase = "멋쟁이 토마토",
        discountPeriod = discountPeriod,
    )

private fun ongoingDiscount(): DiscountPeriod = DiscountPeriod(LocalDate.now().minusDays(1), LocalDate.now().plusDays(30))

private fun sampleProduct(
    id: Long,
    marketId: Long,
    dealType: DealType = DealType.PERIODIC,
    discountPeriod: DiscountPeriod = DiscountPeriod(LocalDate.of(2025, 8, 1), LocalDate.of(2025, 8, 16)),
    category: ProductCategory = ProductCategory.MEAT_EGG,
): Product =
    Product(
        id = id,
        marketId = marketId,
        name = "삼겹살 500g",
        dealType = dealType,
        thumbnailUrl = "https://cdn.example.com/products/$id.png",
        price = Price(BigDecimal("15000"), BigDecimal("12000")),
        category = category,
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

    override fun findNearby(
        condition: NearbyMarketSearchCondition,
        limit: Int,
    ): List<NearbyMarket> = emptyList()

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

    override fun update(product: Product) {
        store[product.id] = product
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

    override fun findLatestActiveByMarketIds(
        marketIds: List<Long>,
        date: LocalDate,
        limitPerMarket: Int,
    ): List<Product> =
        store.values
            .filter { it.marketId in marketIds && isActiveOn(it, date) }
            .groupBy { it.marketId }
            .flatMap { (_, products) -> products.sortedByDescending { it.id }.take(limitPerMarket) }

    override fun countActiveByMarketIds(
        marketIds: List<Long>,
        date: LocalDate,
    ): Map<Long, Int> =
        store.values
            .filter { it.marketId in marketIds && isActiveOn(it, date) }
            .groupingBy { it.marketId }
            .eachCount()

    override fun findActiveByMarketIdAndDealTypeAndCategory(
        marketId: Long,
        dealType: DealType,
        condition: PeriodicProductSearchCondition,
        date: LocalDate,
        limit: Int,
    ): List<Product> =
        store.values
            .filter {
                it.marketId == marketId &&
                    it.dealType == dealType &&
                    isActiveOn(it, date) &&
                    (condition.category == null || it.category == condition.category) &&
                    (condition.cursor == null || it.id < condition.cursor)
            }.sortedByDescending { it.id }
            .take(limit)

    override fun findActiveByLatest(
        marketId: Long,
        condition: ProductListSearchCondition,
        date: LocalDate,
        limit: Int,
    ): List<ProductListItem> =
        activeListItems(marketId, condition, date)
            .sortedByDescending { it.product.id }
            .take(limit)

    override fun findActiveByViewCount(
        marketId: Long,
        condition: ProductListSearchCondition,
        date: LocalDate,
        cursorViewCount: Int?,
        limit: Int,
    ): List<ProductListItem> =
        activeListItems(marketId, condition, date)
            .sortedWith(compareByDescending<ProductListItem> { it.viewCount }.thenByDescending { it.product.id })
            .take(limit)

    override fun findActiveByCategoryOrder(
        marketId: Long,
        condition: ProductListSearchCondition,
        date: LocalDate,
        cursorCategoryOrder: Int?,
        limit: Int,
    ): List<ProductListItem> =
        activeListItems(marketId, condition, date)
            .sortedWith(compareBy<ProductListItem> { it.product.category.ordinal }.thenByDescending { it.product.id })
            .take(limit)

    override fun findListCursorAnchor(
        cursor: Long,
        marketId: Long,
    ): ProductListCursorAnchor? = store[cursor]?.let { ProductListCursorAnchor(categoryOrder = it.category.ordinal, viewCount = 0) }

    private fun activeListItems(
        marketId: Long,
        condition: ProductListSearchCondition,
        date: LocalDate,
    ): List<ProductListItem> =
        store.values
            .filter {
                it.marketId == marketId &&
                    it.dealType == condition.dealType &&
                    isActiveOn(it, date) &&
                    (condition.category == null || it.category == condition.category) &&
                    (condition.cursor == null || it.id < condition.cursor)
            }.map { ProductListItem(it, viewCount = 0, createdAt = LocalDateTime.of(2025, 1, 1, 0, 0)) }

    private fun isActiveOn(
        product: Product,
        date: LocalDate,
    ): Boolean = product.discountPeriod.discountStartDate <= date && product.discountPeriod.discountEndDate >= date
}
