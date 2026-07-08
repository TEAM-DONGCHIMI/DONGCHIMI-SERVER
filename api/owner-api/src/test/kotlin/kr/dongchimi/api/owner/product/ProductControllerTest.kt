package kr.dongchimi.api.owner.product

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kr.dongchimi.api.owner.OwnerApiUser
import kr.dongchimi.api.owner.product.request.ProductBulkDeleteRequest
import kr.dongchimi.api.owner.product.request.ProductDiscountPeriodUpdateRequest
import kr.dongchimi.api.owner.product.request.ProductResetRequest
import kr.dongchimi.core.product.DealType
import kr.dongchimi.core.product.DiscountPeriod
import kr.dongchimi.core.product.Price
import kr.dongchimi.core.product.Product
import kr.dongchimi.core.product.ProductCategory
import kr.dongchimi.core.product.ProductService
import org.mockito.Mockito
import java.math.BigDecimal
import java.time.LocalDate

class ProductControllerTest :
    FunSpec({
        val apiUser = OwnerApiUser(userId = 1L, roles = setOf("OWNER"))
        val marketId = 10L

        test("상세 조회 시 서비스 결과를 응답 data로 반환한다") {
            val productService = Mockito.mock(ProductService::class.java)
            Mockito.`when`(productService.getProduct(1L, marketId, 5L)).thenReturn(sampleProduct())
            val controller = OwnerProductController(productService)

            val response = controller.getDetail(apiUser, marketId, 5L)

            response.success shouldBe true
            response.data!!.productId shouldBe 5L
            response.data!!.categoryName shouldBe "정육/달걀"
            Mockito.verify(productService).getProduct(1L, marketId, 5L)
        }

        test("삭제 시 userId·marketId·productId로 서비스를 호출한다") {
            val productService = Mockito.mock(ProductService::class.java)
            val controller = OwnerProductController(productService)

            val response = controller.delete(apiUser, marketId, 5L)

            response.success shouldBe true
            Mockito.verify(productService).delete(1L, marketId, 5L)
        }

        test("일괄 삭제 시 검증된 productIds로 서비스를 호출한다") {
            val productService = Mockito.mock(ProductService::class.java)
            val controller = OwnerProductController(productService)
            val request = ProductBulkDeleteRequest(productIds = listOf(1L, 2L, 3L))

            val response = controller.deleteAll(apiUser, marketId, request)

            response.success shouldBe true
            Mockito.verify(productService).deleteAll(1L, marketId, listOf(1L, 2L, 3L))
        }

        test("기간 일괄 수정 시 productIds와 할인 기간으로 서비스를 호출한다") {
            val productService = Mockito.mock(ProductService::class.java)
            val controller = OwnerProductController(productService)
            val request =
                ProductDiscountPeriodUpdateRequest(
                    discountStartDate = LocalDate.of(2025, 8, 1),
                    discountEndDate = LocalDate.of(2025, 8, 16),
                    productIds = listOf(1L),
                )

            val response = controller.updateDiscountPeriod(apiUser, marketId, request)

            response.success shouldBe true
            Mockito.verify(productService).updateDiscountPeriod(1L, marketId, request.productIds, request.toDiscountPeriod())
        }

        test("초기화 시 dealType으로 서비스를 호출한다") {
            val productService = Mockito.mock(ProductService::class.java)
            val controller = OwnerProductController(productService)
            val request = ProductResetRequest(dealType = DealType.DAILY)

            val response = controller.reset(apiUser, marketId, request)

            response.success shouldBe true
            Mockito.verify(productService).reset(1L, marketId, DealType.DAILY)
        }
    })

private fun sampleProduct(): Product =
    Product(
        id = 5L,
        marketId = 10L,
        name = "삼겹살 500g",
        dealType = DealType.PERIODIC,
        thumbnailUrl = "https://cdn.example.com/products/5.png",
        price = Price(BigDecimal("15000"), BigDecimal("12000")),
        category = ProductCategory.MEAT_EGG,
        promotionalPhrase = null,
        discountPeriod = DiscountPeriod(LocalDate.of(2025, 8, 1), LocalDate.of(2025, 8, 16)),
    )
