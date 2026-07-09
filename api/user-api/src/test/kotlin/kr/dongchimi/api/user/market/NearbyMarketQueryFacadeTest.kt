package kr.dongchimi.api.user.market

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kr.dongchimi.core.common.CursorSliceResult
import kr.dongchimi.core.market.BusinessHourSlot
import kr.dongchimi.core.market.BusinessHours
import kr.dongchimi.core.market.LocationPoint
import kr.dongchimi.core.market.Market
import kr.dongchimi.core.market.MarketInfo
import kr.dongchimi.core.market.MarketPhoneNumber
import kr.dongchimi.core.market.MarketService
import kr.dongchimi.core.market.NearbyMarket
import kr.dongchimi.core.market.NearbyMarketSearchCondition
import kr.dongchimi.core.product.DealType
import kr.dongchimi.core.product.DiscountPeriod
import kr.dongchimi.core.product.Price
import kr.dongchimi.core.product.Product
import kr.dongchimi.core.product.ProductCategory
import kr.dongchimi.core.product.ProductService
import org.mockito.Mockito
import java.math.BigDecimal
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class NearbyMarketQueryFacadeTest :
    FunSpec({
        // 2026-07-06은 월요일
        val now = LocalDateTime.of(2026, 7, 6, 14, 0)
        val today: LocalDate = now.toLocalDate()
        val condition =
            NearbyMarketSearchCondition(
                origin = LocationPoint(longitude = 126.98, latitude = 37.55),
                radiusMeters = 1000.0,
                cursorMarketId = null,
                size = 2,
            )

        fun facadeWith(
            slice: CursorSliceResult<NearbyMarket>,
            previewProducts: List<Product> = emptyList(),
            counts: Map<Long, Int> = emptyMap(),
        ): NearbyMarketQueryFacade {
            val marketService = Mockito.mock(MarketService::class.java)
            val productService = Mockito.mock(ProductService::class.java)
            val marketIds = slice.content.map { it.market.id }
            Mockito.`when`(marketService.getNearbyMarkets(condition)).thenReturn(slice)
            Mockito.`when`(productService.getLatestActiveProducts(marketIds, today, 3)).thenReturn(previewProducts)
            Mockito.`when`(productService.countActiveProductsByMarketIds(marketIds, today)).thenReturn(counts)

            return NearbyMarketQueryFacade(marketService, productService)
        }

        test("마트와 상품을 조합해 목록 응답을 만든다") {
            val slice = CursorSliceResult(listOf(nearbyMarket(1L), nearbyMarket(2L)), hasNext = false)
            val facade =
                facadeWith(
                    slice = slice,
                    previewProducts = listOf(product(101L, marketId = 1L), product(102L, marketId = 1L)),
                    counts = mapOf(1L to 6),
                )

            val response = facade.getNearbyMarkets(condition, now)

            response.content.map { it.marketId } shouldBe listOf(1L, 2L)
            response.content.first().slug shouldBe "slug-1"
            response.content.first().productCount shouldBe 6
            response.content
                .first()
                .previewProducts
                .map { it.productId } shouldBe listOf(101L, 102L)
            response.content.first().isOpen shouldBe true
        }

        test("hasNext가 true면 마지막 마트 id를 nextCursor로 내려준다") {
            val slice = CursorSliceResult(listOf(nearbyMarket(1L), nearbyMarket(2L)), hasNext = true, nextCursor = 2L)

            val response = facadeWith(slice).getNearbyMarkets(condition, now)

            response.hasNext shouldBe true
            response.nextCursor shouldBe 2L
        }

        test("hasNext가 false면 nextCursor는 null이다") {
            val slice = CursorSliceResult(listOf(nearbyMarket(1L)), hasNext = false)

            val response = facadeWith(slice).getNearbyMarkets(condition, now)

            response.hasNext shouldBe false
            response.nextCursor shouldBe null
        }

        test("상품이 없는 마트는 productCount 0과 빈 미리보기를 갖는다") {
            val slice = CursorSliceResult(listOf(nearbyMarket(1L)), hasNext = false)

            val response = facadeWith(slice).getNearbyMarkets(condition, now)

            response.content.first().productCount shouldBe 0
            response.content.first().previewProducts shouldBe emptyList()
        }

        test("상세 주소는 잘라내고 기본 주소만 내려준다") {
            val slice = CursorSliceResult(listOf(nearbyMarket(1L, address = "서울시 마포구 망원동|101동 202호")), hasNext = false)

            val response = facadeWith(slice).getNearbyMarkets(condition, now)

            response.content.first().address shouldBe "서울시 마포구 망원동"
        }

        test("반경 내 마트가 없으면 빈 목록을 반환한다") {
            val slice = CursorSliceResult(emptyList<NearbyMarket>(), hasNext = false)

            val response = facadeWith(slice).getNearbyMarkets(condition, now)

            response.content shouldBe emptyList()
            response.hasNext shouldBe false
            response.nextCursor shouldBe null
        }
    })

private fun nearbyMarket(
    id: Long,
    address: String = "서울시 마포구 망원동",
) = NearbyMarket(
    market =
        Market(
            id = id,
            ownerId = 1L,
            info = MarketInfo(name = "마트$id", address = address, thumbnailUrl = null),
            location = LocationPoint(longitude = 126.98, latitude = 37.55),
            businessHours =
                BusinessHours(
                    listOf(
                        BusinessHourSlot(
                            days = listOf(DayOfWeek.MONDAY),
                            isOpen = true,
                            open = LocalTime.of(10, 0),
                            close = LocalTime.of(20, 0),
                        ),
                    ),
                ),
            phoneNumber = MarketPhoneNumber("02-123-4567", null, 1, "010-1234-5678"),
            brn = null,
        ),
    slug = "slug-$id",
)

private fun product(
    id: Long,
    marketId: Long,
) = Product(
    id = id,
    marketId = marketId,
    name = "삼겹살 500g",
    dealType = DealType.DAILY,
    thumbnailUrl = "https://cdn.example.com/products/$id.png",
    price = Price(BigDecimal("7700"), BigDecimal("6900")),
    category = ProductCategory.MEAT_EGG,
    promotionalPhrase = null,
    discountPeriod = DiscountPeriod(LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 31)),
)
