package kr.dongchimi.api.user.product

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

class ProductDetailQueryFacadeTest :
    FunSpec({
        val marketId = 10L
        val productId = 1L

        test("마트와 상품을 조합해 상세 응답을 만든다") {
            val marketService = Mockito.mock(MarketService::class.java)
            val productService = Mockito.mock(ProductService::class.java)
            Mockito.`when`(marketService.getById(marketId)).thenReturn(market())
            Mockito.`when`(productService.getDetail(marketId, productId)).thenReturn(product())

            val facade = ProductDetailQueryFacade(marketService, productService)

            val response = facade.getDetail(marketId, productId)

            response.productId shouldBe productId
            response.marketName shouldBe "망원 신선마트"
            response.discountRate shouldBe 20
        }
    })

private fun market() =
    Market(
        id = 10L,
        ownerId = 1L,
        info = MarketInfo(name = "망원 신선마트", address = "서울시 마포구 망원동", thumbnailUrl = null),
        location = LocationPoint(longitude = 127.0, latitude = 37.5),
        businessHours = BusinessHours(emptyList()),
        phoneNumber =
            MarketPhoneNumber(
                marketPhone1 = "02-123-4567",
                marketPhone2 = null,
                marketPhonePrimary = 1,
                ownerPhone = "010-1234-5678",
            ),
        brn = null,
    )

private fun product() =
    Product(
        id = 1L,
        marketId = 10L,
        name = "삼겹살 500g",
        dealType = DealType.PERIODIC,
        thumbnailUrl = "https://cdn.example.com/products/1.png",
        price = Price(originalPrice = BigDecimal(15000), discountedPrice = BigDecimal(12000)),
        category = ProductCategory.MEAT_EGG,
        promotionalPhrase = "오늘만 특가!",
        discountPeriod = DiscountPeriod(discountStartDate = LocalDate.of(2025, 8, 1), discountEndDate = LocalDate.of(2025, 8, 16)),
    )
