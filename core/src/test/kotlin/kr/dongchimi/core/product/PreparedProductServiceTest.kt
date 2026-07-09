package kr.dongchimi.core.product

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import kr.dongchimi.core.common.PageOffset
import kr.dongchimi.core.common.exception.CoreException
import kr.dongchimi.core.market.BusinessHours
import kr.dongchimi.core.market.LocationPoint
import kr.dongchimi.core.market.Market
import kr.dongchimi.core.market.MarketErrorCode
import kr.dongchimi.core.market.MarketInfo
import kr.dongchimi.core.market.MarketPhoneNumber
import kr.dongchimi.core.market.MarketRepository
import kr.dongchimi.core.market.MarketValidator
import java.math.BigDecimal
import java.time.LocalDate

class PreparedProductServiceTest :
    FunSpec({
        val ownerId = 1L
        val marketId = 10L
        val condition = PreparedProductSearchCondition(search = null, categories = emptyList())
        val pageOffset = PageOffset(PageOffset.DEFAULT_PAGE, PageOffset.DEFAULT_SIZE)

        fun newService(
            markets: PreparedProductFakeMarketRepository = PreparedProductFakeMarketRepository(),
            preparedProducts: FakePreparedProductRepository = FakePreparedProductRepository(),
            products: PreparedProductFakeProductRepository = PreparedProductFakeProductRepository(),
        ): PreparedProductService =
            PreparedProductService(
                marketValidator = MarketValidator(markets),
                preparedProductFinder = PreparedProductFinder(preparedProducts),
                preparedProductValidator = PreparedProductValidator(preparedProducts),
                preparedProductUpdater = PreparedProductUpdater(preparedProducts, DraftFailReasonResolver()),
                preparedProductConfirmer =
                    PreparedProductConfirmer(
                        products,
                        preparedProducts,
                        preparedProductValidator,
                    ),
            )

        test("목록 조회: 마트가 없으면 MARKET_NOT_FOUND") {
            val service = newService()

            val exception = shouldThrow<CoreException> { service.getDrafts(ownerId, marketId, condition, pageOffset) }

            exception.errorCode shouldBe MarketErrorCode.MARKET_NOT_FOUND
        }

        test("목록 조회: 다른 점주 소유 마트면 MARKET_ACCESS_DENIED") {
            val markets = PreparedProductFakeMarketRepository().apply { put(marketId, ownerId = 2L) }
            val service = newService(markets)

            val exception = shouldThrow<CoreException> { service.getDrafts(ownerId, marketId, condition, pageOffset) }

            exception.errorCode shouldBe MarketErrorCode.MARKET_ACCESS_DENIED
        }

        test("목록 조회: 정상이면 finder가 반환한 목록을 그대로 반환한다") {
            val markets = PreparedProductFakeMarketRepository().apply { put(marketId, ownerId) }
            val preparedProducts =
                FakePreparedProductRepository().apply {
                    put(samplePreparedProduct(id = 1L, marketId = marketId))
                }
            val service = newService(markets, preparedProducts)

            val drafts = service.getDrafts(ownerId, marketId, condition, pageOffset)

            drafts.map { it.id } shouldBe listOf(1L)
        }

        test("카운트 조회: 마트가 없으면 MARKET_NOT_FOUND") {
            val service = newService()

            val exception = shouldThrow<CoreException> { service.getDraftCounts(ownerId, marketId) }

            exception.errorCode shouldBe MarketErrorCode.MARKET_NOT_FOUND
        }

        test("카운트 조회: 다른 점주 소유 마트면 MARKET_ACCESS_DENIED") {
            val markets = PreparedProductFakeMarketRepository().apply { put(marketId, ownerId = 2L) }
            val service = newService(markets)

            val exception = shouldThrow<CoreException> { service.getDraftCounts(ownerId, marketId) }

            exception.errorCode shouldBe MarketErrorCode.MARKET_ACCESS_DENIED
        }

        test("카운트 조회: 정상이면 finder가 반환한 카운트를 그대로 반환한다") {
            val markets = PreparedProductFakeMarketRepository().apply { put(marketId, ownerId) }
            val preparedProducts =
                FakePreparedProductRepository().apply {
                    put(samplePreparedProduct(id = 1L, marketId = marketId, draftStatus = DraftStatus.SUCCESS))
                    put(samplePreparedProduct(id = 2L, marketId = marketId, draftStatus = DraftStatus.FAIL))
                }
            val service = newService(markets, preparedProducts)

            val counts = service.getDraftCounts(ownerId, marketId)

            counts.totalCount shouldBe 2L
            counts.successCount shouldBe 1L
            counts.failCount shouldBe 1L
        }

        test("임시저장: 다른 점주 소유 마트면 MARKET_ACCESS_DENIED") {
            val markets = PreparedProductFakeMarketRepository().apply { put(marketId, ownerId = 2L) }
            val service = newService(markets)

            val exception = shouldThrow<CoreException> { service.saveDrafts(ownerId, marketId, listOf(completeCommand(1L))) }

            exception.errorCode shouldBe MarketErrorCode.MARKET_ACCESS_DENIED
        }

        test("임시저장: 다른 마트의 임시저장 상품이 섞여 있으면 PREPARED_PRODUCT_NOT_FOUND") {
            val markets = PreparedProductFakeMarketRepository().apply { put(marketId, ownerId) }
            val preparedProducts =
                FakePreparedProductRepository().apply {
                    put(samplePreparedProduct(id = 1L, marketId = marketId))
                    put(samplePreparedProduct(id = 2L, marketId = 99L))
                }
            val service = newService(markets, preparedProducts)

            val exception =
                shouldThrow<CoreException> {
                    service.saveDrafts(ownerId, marketId, listOf(completeCommand(1L), completeCommand(2L)))
                }

            exception.errorCode shouldBe PreparedProductErrorCode.PREPARED_PRODUCT_NOT_FOUND
        }

        test("임시저장: 필수값을 모두 채우면 SUCCESS가 되고 failReason이 지워진다") {
            val markets = PreparedProductFakeMarketRepository().apply { put(marketId, ownerId) }
            val preparedProducts =
                FakePreparedProductRepository().apply {
                    put(
                        samplePreparedProduct(id = 1L, marketId = marketId, draftStatus = DraftStatus.FAIL)
                            .copy(failReason = DraftFailReason.THUMBNAIL_MISSING),
                    )
                }
            val service = newService(markets, preparedProducts)

            service.saveDrafts(ownerId, marketId, listOf(completeCommand(1L)))

            preparedProducts.get(1L).draftStatus shouldBe DraftStatus.SUCCESS
            preparedProducts.get(1L).failReason shouldBe null
        }

        test("임시저장: 필수값이 비면 FAIL이 되고 우선순위가 가장 높은 사유가 저장된다") {
            val markets = PreparedProductFakeMarketRepository().apply { put(marketId, ownerId) }
            val preparedProducts = FakePreparedProductRepository().apply { put(samplePreparedProduct(id = 1L, marketId = marketId)) }
            val service = newService(markets, preparedProducts)

            // 이미지와 상품명이 동시에 누락 → 우선순위상 이미지 누락
            service.saveDrafts(ownerId, marketId, listOf(completeCommand(1L).copy(thumbnailUrl = null, name = null)))

            preparedProducts.get(1L).draftStatus shouldBe DraftStatus.FAIL
            preparedProducts.get(1L).failReason shouldBe DraftFailReason.THUMBNAIL_MISSING
        }

        test("임시저장: dealType을 보내지 않으면 PERIODIC으로 저장된다") {
            val markets = PreparedProductFakeMarketRepository().apply { put(marketId, ownerId) }
            val preparedProducts = FakePreparedProductRepository().apply { put(samplePreparedProduct(id = 1L, marketId = marketId)) }
            val service = newService(markets, preparedProducts)

            service.saveDrafts(ownerId, marketId, listOf(completeCommand(1L)))

            preparedProducts.get(1L).dealType shouldBe DealType.PERIODIC
        }

        test("임시저장: 요청에 없는 기존 임시저장 상품은 변경되지 않는다") {
            val markets = PreparedProductFakeMarketRepository().apply { put(marketId, ownerId) }
            val untouched = samplePreparedProduct(id = 2L, marketId = marketId, draftStatus = DraftStatus.FAIL)
            val preparedProducts =
                FakePreparedProductRepository().apply {
                    put(samplePreparedProduct(id = 1L, marketId = marketId))
                    put(untouched)
                }
            val service = newService(markets, preparedProducts)

            service.saveDrafts(ownerId, marketId, listOf(completeCommand(1L)))

            preparedProducts.get(2L) shouldBe untouched
        }

        test("최종 저장: 다른 점주 소유 마트면 MARKET_ACCESS_DENIED") {
            val markets = PreparedProductFakeMarketRepository().apply { put(marketId, ownerId = 2L) }
            val service = newService(markets)

            val exception = shouldThrow<CoreException> { service.confirmDrafts(ownerId, marketId) }

            exception.errorCode shouldBe MarketErrorCode.MARKET_ACCESS_DENIED
        }

        test("최종 저장: 모두 SUCCESS면 상품으로 등록하고 원본 임시저장 상품을 삭제한다") {
            val markets = PreparedProductFakeMarketRepository().apply { put(marketId, ownerId) }
            val preparedProducts =
                FakePreparedProductRepository().apply {
                    put(completePreparedProduct(id = 1L, marketId = marketId))
                    put(completePreparedProduct(id = 2L, marketId = marketId))
                }
            val products = PreparedProductFakeProductRepository()
            val service = newService(markets, preparedProducts, products)

            service.confirmDrafts(ownerId, marketId)

            products.saved.map { it.name } shouldBe listOf("삼겹살 500g", "삼겹살 500g")
            preparedProducts.deletedIds shouldBe listOf(1L, 2L)
        }

        test("최종 저장: 하나라도 FAIL이면 DRAFT_NOT_COMPLETED이고 아무것도 등록되지 않는다") {
            val markets = PreparedProductFakeMarketRepository().apply { put(marketId, ownerId) }
            val preparedProducts =
                FakePreparedProductRepository().apply {
                    put(completePreparedProduct(id = 1L, marketId = marketId))
                    put(samplePreparedProduct(id = 2L, marketId = marketId, draftStatus = DraftStatus.FAIL))
                }
            val products = PreparedProductFakeProductRepository()
            val service = newService(markets, preparedProducts, products)

            val exception = shouldThrow<CoreException> { service.confirmDrafts(ownerId, marketId) }

            exception.errorCode shouldBe PreparedProductErrorCode.DRAFT_NOT_COMPLETED
            products.saved.shouldBeEmpty()
            preparedProducts.deletedIds.shouldBeEmpty()
        }

        test("최종 저장: 임시저장 상품이 없으면 아무것도 하지 않고 성공한다") {
            val markets = PreparedProductFakeMarketRepository().apply { put(marketId, ownerId) }
            val products = PreparedProductFakeProductRepository()
            val service = newService(markets, products = products)

            service.confirmDrafts(ownerId, marketId)

            products.saved.shouldBeEmpty()
        }

        test("최종 저장: 이미 삭제된 임시저장 상품은 검증·이관 대상에서 제외된다") {
            val markets = PreparedProductFakeMarketRepository().apply { put(marketId, ownerId) }
            val preparedProducts =
                FakePreparedProductRepository().apply {
                    put(completePreparedProduct(id = 1L, marketId = marketId))
                    put(samplePreparedProduct(id = 2L, marketId = marketId, draftStatus = DraftStatus.FAIL))
                    softDeleteByIds(listOf(2L))
                }
            val products = PreparedProductFakeProductRepository()
            val service = newService(markets, preparedProducts, products)

            service.confirmDrafts(ownerId, marketId)

            products.saved.map { it.name } shouldBe listOf("삼겹살 500g")
        }
    })

private fun samplePreparedProduct(
    id: Long,
    marketId: Long,
    draftStatus: DraftStatus = DraftStatus.SUCCESS,
): PreparedProduct =
    PreparedProduct(
        id = id,
        marketId = marketId,
        name = "삼겹살 500g",
        thumbnailUrl = null,
        price = null,
        category = ProductCategory.MEAT_EGG,
        promotionalPhrase = null,
        discountPeriod = null,
        draftStatus = draftStatus,
        failReason = null,
    )

/** 필수값이 모두 채워져 상품으로 등록 가능한 초안 */
private fun completePreparedProduct(
    id: Long,
    marketId: Long,
): PreparedProduct =
    PreparedProduct(
        id = id,
        marketId = marketId,
        name = "삼겹살 500g",
        thumbnailUrl = "https://static.dongchimi.kr/test.png",
        price = Price(BigDecimal("5000"), BigDecimal("4000")),
        category = ProductCategory.MEAT_EGG,
        promotionalPhrase = null,
        discountPeriod = DiscountPeriod(LocalDate.of(2025, 8, 1), LocalDate.of(2025, 8, 16)),
        dealType = DealType.PERIODIC,
        draftStatus = DraftStatus.SUCCESS,
        failReason = null,
    )

private fun completeCommand(
    id: Long,
    dealType: DealType? = null,
): PreparedProductDraftSaveCommand =
    PreparedProductDraftSaveCommand(
        id = id,
        name = "삼겹살 500g",
        thumbnailUrl = "https://static.dongchimi.kr/test.png",
        price = Price(BigDecimal("5000"), BigDecimal("4000")),
        category = ProductCategory.MEAT_EGG,
        promotionalPhrase = null,
        discountPeriod = DiscountPeriod(LocalDate.of(2025, 8, 1), LocalDate.of(2025, 8, 16)),
        dealType = dealType ?: DealType.PERIODIC,
    )

private class PreparedProductFakeMarketRepository : MarketRepository {
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

private class FakePreparedProductRepository : PreparedProductRepository {
    private val store = mutableMapOf<Long, PreparedProduct>()
    val deletedIds = mutableListOf<Long>()

    fun put(preparedProduct: PreparedProduct) {
        store[preparedProduct.id] = preparedProduct
    }

    fun get(id: Long): PreparedProduct = store.getValue(id)

    private fun alive(): List<PreparedProduct> = store.values.filterNot { it.id in deletedIds }

    override fun findDrafts(
        marketId: Long,
        condition: PreparedProductSearchCondition,
        pageOffset: PageOffset,
    ): List<PreparedProduct> = alive().filter { it.marketId == marketId }

    override fun countDrafts(marketId: Long): PreparedProductDraftCounts {
        val products = alive().filter { it.marketId == marketId }
        val successCount = products.count { it.draftStatus == DraftStatus.SUCCESS }.toLong()
        val failCount = products.count { it.draftStatus == DraftStatus.FAIL }.toLong()

        return PreparedProductDraftCounts(
            totalCount = successCount + failCount,
            successCount = successCount,
            failCount = failCount,
        )
    }

    override fun findAllByMarketId(marketId: Long): List<PreparedProduct> = alive().filter { it.marketId == marketId }

    override fun countInMarket(
        ids: List<Long>,
        marketId: Long,
    ): Int = alive().count { it.id in ids && it.marketId == marketId }

    override fun updateDrafts(
        commands: List<PreparedProductDraftSaveCommand>,
        failReasons: Map<Long, DraftFailReason?>,
    ) {
        commands.forEach { command ->
            val failReason = failReasons[command.id]
            store[command.id] =
                store.getValue(command.id).copy(
                    name = command.name,
                    thumbnailUrl = command.thumbnailUrl,
                    price = command.price,
                    category = command.category,
                    promotionalPhrase = command.promotionalPhrase,
                    discountPeriod = command.discountPeriod,
                    dealType = command.dealType,
                    failReason = failReason,
                    draftStatus = if (failReason == null) DraftStatus.SUCCESS else DraftStatus.FAIL,
                )
        }
    }

    override fun softDeleteByIds(ids: List<Long>) {
        deletedIds += ids
    }
}

private class PreparedProductFakeProductRepository : ProductRepository {
    val saved = mutableListOf<Product>()

    override fun saveAll(products: List<Product>): List<Product> {
        saved += products
        return products
    }

    override fun findById(id: Long): Product? = null

    override fun findAllByIds(ids: List<Long>): List<Product> = emptyList()

    override fun findAllByMarketIdAndDealType(
        marketId: Long,
        dealType: DealType,
    ): List<Product> = emptyList()

    override fun save(product: Product): Product = product

    override fun softDeleteByIds(ids: List<Long>) = Unit

    override fun softDeleteByMarketIdAndDealType(
        marketId: Long,
        dealType: DealType,
    ) = Unit

    override fun countProductsInMarket(
        productIds: List<Long>,
        marketId: Long,
    ): Int = 0

    override fun updateDiscountPeriod(
        productIds: List<Long>,
        discountPeriod: DiscountPeriod,
    ) = Unit

    override fun findActiveByMarketIdAndDealType(
        marketId: Long,
        dealType: DealType,
        date: LocalDate,
        limit: Int,
    ): List<Product> = emptyList()

    override fun countActiveByMarketIdAndDealType(
        marketId: Long,
        dealType: DealType,
        date: LocalDate,
    ): Int = 0

    override fun countRegisteredOn(
        marketId: Long,
        date: LocalDate,
    ): Int = 0
}
