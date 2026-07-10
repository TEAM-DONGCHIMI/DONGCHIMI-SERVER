package kr.dongchimi.api.owner.flyer

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kr.dongchimi.core.market.BusinessHourSlot
import kr.dongchimi.core.market.BusinessHours
import kr.dongchimi.core.market.LocationPoint
import kr.dongchimi.core.market.Market
import kr.dongchimi.core.market.MarketInfo
import kr.dongchimi.core.market.MarketPhoneNumber
import kr.dongchimi.core.market.MarketService
import kr.dongchimi.core.product.DealType
import kr.dongchimi.core.product.DiscountPeriod
import kr.dongchimi.core.product.DraftStatus
import kr.dongchimi.core.product.PreparedProduct
import kr.dongchimi.core.product.PreparedProductService
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

class FlyerPreviewQueryFacadeTest :
    FunSpec({
        val ownerId = 1L
        val marketId = 10L
        // 2026-07-06은 월요일
        val now = LocalDateTime.of(2026, 7, 6, 14, 0)

        test("마트/인기상품/오늘의특가/임시저장 상품을 조합해 미리보기 응답을 만든다") {
            val marketService = Mockito.mock(MarketService::class.java)
            val productService = Mockito.mock(ProductService::class.java)
            val preparedProductService = Mockito.mock(PreparedProductService::class.java)
            Mockito.`when`(marketService.getByIdForOwner(ownerId, marketId)).thenReturn(market())
            Mockito
                .`when`(productService.getPopularActiveProducts(marketId, now.toLocalDate(), 3))
                .thenReturn(listOf(product(101L)))
            Mockito
                .`when`(productService.getAllActiveProducts(marketId, DealType.DAILY, now.toLocalDate()))
                .thenReturn(listOf(product(201L)))
            Mockito
                .`when`(preparedProductService.getPreviewDrafts(ownerId, marketId))
                .thenReturn(listOf(preparedProduct(301L)))

            val facade = FlyerPreviewQueryFacade(marketService, productService, preparedProductService)

            val response = facade.getPeriodicPreview(ownerId, marketId, now)

            response.marketId shouldBe marketId
            response.address shouldBe "서울시 마포구 망원동"
            response.isOpenNow shouldBe true
            response.businessHours.first().days shouldBe listOf("MONDAY", "TUESDAY")
            response.businessHours.first().open shouldBe "10:00"
            response.top3.map { it.productId } shouldBe listOf(101L)
            response.daily.totalCount shouldBe 1
            response.daily.products.map { it.productId } shouldBe listOf(201L)
            response.preparedProducts.map { it.preparedProductId } shouldBe listOf(301L)
        }

        test("영업하지 않는 요일이면 isOpenNow는 false다") {
            val marketService = Mockito.mock(MarketService::class.java)
            val productService = Mockito.mock(ProductService::class.java)
            val preparedProductService = Mockito.mock(PreparedProductService::class.java)
            // 2026-07-08은 수요일 (영업 슬롯 없음)
            val wednesday = LocalDateTime.of(2026, 7, 8, 14, 0)
            Mockito.`when`(marketService.getByIdForOwner(ownerId, marketId)).thenReturn(market())
            Mockito
                .`when`(productService.getPopularActiveProducts(marketId, wednesday.toLocalDate(), 3))
                .thenReturn(emptyList())
            Mockito
                .`when`(productService.getAllActiveProducts(marketId, DealType.DAILY, wednesday.toLocalDate()))
                .thenReturn(emptyList())
            Mockito
                .`when`(preparedProductService.getPreviewDrafts(ownerId, marketId))
                .thenReturn(emptyList())

            val facade = FlyerPreviewQueryFacade(marketService, productService, preparedProductService)

            val response = facade.getPeriodicPreview(ownerId, marketId, wednesday)

            response.isOpenNow shouldBe false
            response.top3 shouldBe emptyList()
        }
    })

private fun market() =
    Market(
        id = 10L,
        ownerId = 1L,
        info =
            MarketInfo(
                name = "망원 신선마트",
                address = "서울시 마포구 망원동|101호",
                thumbnailUrl = null,
            ),
        location = LocationPoint(longitude = 127.0, latitude = 37.5),
        businessHours =
            BusinessHours(
                listOf(
                    BusinessHourSlot(
                        days = listOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY),
                        isOpen = true,
                        open = LocalTime.of(10, 0),
                        close = LocalTime.of(20, 0),
                    ),
                    BusinessHourSlot(days = listOf(DayOfWeek.SUNDAY), isOpen = false),
                ),
            ),
        phoneNumber =
            MarketPhoneNumber(
                marketPhone1 = "02-123-4567",
                marketPhone2 = null,
                marketPhonePrimary = 1,
                ownerPhone = "010-1234-5678",
            ),
        brn = null,
    )

private fun product(id: Long) =
    Product(
        id = id,
        marketId = 10L,
        name = "삼겹살 500g",
        dealType = DealType.DAILY,
        thumbnailUrl = "https://static.dongchimi.kr/products/$id.png",
        price = Price(originalPrice = BigDecimal(10000), discountedPrice = BigDecimal(9000)),
        category = ProductCategory.MEAT_EGG,
        promotionalPhrase = null,
        discountPeriod = DiscountPeriod(discountStartDate = LocalDate.now(), discountEndDate = LocalDate.now()),
    )

private fun preparedProduct(id: Long) =
    PreparedProduct(
        id = id,
        marketId = 10L,
        name = "바나나 한 송이",
        thumbnailUrl = "https://static.dongchimi.kr/prepared-products/$id.png",
        price = Price(originalPrice = BigDecimal(3200), discountedPrice = BigDecimal(1500)),
        category = ProductCategory.VEGETABLE_FRUIT,
        promotionalPhrase = null,
        discountPeriod = DiscountPeriod(discountStartDate = LocalDate.now(), discountEndDate = LocalDate.now()),
        draftStatus = DraftStatus.SUCCESS,
        failReason = null,
    )
