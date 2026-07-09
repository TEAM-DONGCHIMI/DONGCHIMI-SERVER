package kr.dongchimi.core.product

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
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

class PreparedProductServiceTest :
    FunSpec({
        val ownerId = 1L
        val marketId = 10L
        val condition = PreparedProductSearchCondition(search = null, categories = emptyList())
        val pageOffset = PageOffset(PageOffset.DEFAULT_PAGE, PageOffset.DEFAULT_SIZE)

        fun newService(
            markets: PreparedProductFakeMarketRepository = PreparedProductFakeMarketRepository(),
            preparedProducts: FakePreparedProductRepository = FakePreparedProductRepository(),
        ): PreparedProductService =
            PreparedProductService(
                marketValidator = MarketValidator(markets),
                preparedProductFinder = PreparedProductFinder(preparedProducts),
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
                businessHours = BusinessHours(),
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

    fun put(preparedProduct: PreparedProduct) {
        store[preparedProduct.id] = preparedProduct
    }

    override fun findDrafts(
        marketId: Long,
        condition: PreparedProductSearchCondition,
        pageOffset: PageOffset,
    ): List<PreparedProduct> = store.values.filter { it.marketId == marketId }

    override fun countDrafts(marketId: Long): PreparedProductDraftCounts {
        val products = store.values.filter { it.marketId == marketId }
        val successCount = products.count { it.draftStatus == DraftStatus.SUCCESS }.toLong()
        val failCount = products.count { it.draftStatus == DraftStatus.FAIL }.toLong()

        return PreparedProductDraftCounts(
            totalCount = successCount + failCount,
            successCount = successCount,
            failCount = failCount,
        )
    }
}
