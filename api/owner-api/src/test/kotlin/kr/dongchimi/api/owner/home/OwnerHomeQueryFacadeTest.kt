package kr.dongchimi.api.owner.home

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kr.dongchimi.core.market.BusinessHours
import kr.dongchimi.core.market.LocationPoint
import kr.dongchimi.core.market.Market
import kr.dongchimi.core.market.MarketInfo
import kr.dongchimi.core.market.MarketPhoneNumber
import kr.dongchimi.core.market.MarketService
import kr.dongchimi.core.product.DealType
import kr.dongchimi.core.product.DiscountPeriod
import kr.dongchimi.core.product.Price
import kr.dongchimi.core.product.Product
import kr.dongchimi.core.product.ProductCategory
import kr.dongchimi.core.product.ProductService
import org.mockito.Mockito
import java.math.BigDecimal
import java.time.LocalDate

class OwnerHomeQueryFacadeTest :
    FunSpec({
        val marketId = 10L
        val ownerId = 1L
        val today = LocalDate.now()

        test("마트를 등록한 점주면 마트의 상품 현황을 조합해 응답한다") {
            val marketService = Mockito.mock(MarketService::class.java)
            val productService = Mockito.mock(ProductService::class.java)
            Mockito.`when`(marketService.findByOwnerId(ownerId)).thenReturn(market(marketId))

            val dailyProduct = product(id = 1L, dealType = DealType.DAILY)
            val periodicProduct = product(id = 2L, dealType = DealType.PERIODIC)
            Mockito
                .`when`(productService.getActiveProducts(marketId, DealType.DAILY, today, 4))
                .thenReturn(listOf(dailyProduct))
            Mockito
                .`when`(productService.getActiveProducts(marketId, DealType.PERIODIC, today, 4))
                .thenReturn(listOf(periodicProduct))
            Mockito.`when`(productService.countActiveProducts(marketId, DealType.DAILY, today)).thenReturn(30)
            Mockito.`when`(productService.countActiveProducts(marketId, DealType.PERIODIC, today)).thenReturn(5)
            Mockito.`when`(productService.countRegisteredOn(marketId, today)).thenReturn(10)

            val facade = OwnerHomeQueryFacade(marketService, productService)

            val response = facade.getHome(ownerId)

            response.todayRegisteredCount shouldBe 10
            response.dailyCount shouldBe 30
            response.dailyProducts.map { it.productId } shouldBe listOf(1L)
            response.periodicCount shouldBe 5
            response.periodicProducts.map { it.productId } shouldBe listOf(2L)
        }

        test("마트를 등록하지 않은 점주면 모든 값이 0/빈 배열이다") {
            val marketService = Mockito.mock(MarketService::class.java)
            val productService = Mockito.mock(ProductService::class.java)
            Mockito.`when`(marketService.findByOwnerId(ownerId)).thenReturn(null)

            val facade = OwnerHomeQueryFacade(marketService, productService)

            val response = facade.getHome(ownerId)

            response.todayRegisteredCount shouldBe 0
            response.dailyCount shouldBe 0
            response.dailyProducts shouldBe emptyList()
            response.periodicCount shouldBe 0
            response.periodicProducts shouldBe emptyList()
            Mockito.verifyNoInteractions(productService)
        }
    })

private fun market(marketId: Long) =
    Market(
        id = marketId,
        ownerId = 1L,
        info =
            MarketInfo(
                name = "신선마트",
                address = "서울시 강남구",
                thumbnailUrl = null,
            ),
        location = LocationPoint(longitude = 127.0, latitude = 37.5),
        businessHours = BusinessHours(),
        phoneNumber =
            MarketPhoneNumber(
                marketPhone1 = "0212345678",
                marketPhone2 = null,
                marketPhonePrimary = 1,
                ownerPhone = "01012345678",
            ),
        brn = null,
    )

private fun product(
    id: Long,
    dealType: DealType,
) = Product(
    id = id,
    marketId = 10L,
    name = "풀무원 콩나물 500g",
    dealType = dealType,
    thumbnailUrl = "https://static.ddongchimi.kr/products/test.png",
    price = Price(originalPrice = BigDecimal(5000), discountedPrice = BigDecimal(4500)),
    category = ProductCategory.VEGETABLE_FRUIT,
    promotionalPhrase = null,
    discountPeriod = DiscountPeriod(discountStartDate = LocalDate.now(), discountEndDate = LocalDate.now()),
)
