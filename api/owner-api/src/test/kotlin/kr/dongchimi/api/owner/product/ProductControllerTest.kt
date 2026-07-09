package kr.dongchimi.api.owner.product

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kr.dongchimi.api.core.common.dto.PageOffsetRequest
import kr.dongchimi.api.owner.OwnerApiUser
import kr.dongchimi.api.owner.product.request.PreparedProductDraftSearchRequest
import kr.dongchimi.api.owner.product.request.ProductBulkDeleteRequest
import kr.dongchimi.api.owner.product.request.ProductDiscountPeriodUpdateRequest
import kr.dongchimi.api.owner.product.request.ProductResetRequest
import kr.dongchimi.core.common.PageOffset
import kr.dongchimi.core.product.DealType
import kr.dongchimi.core.product.DiscountPeriod
import kr.dongchimi.core.product.DraftStatus
import kr.dongchimi.core.product.PreparedProduct
import kr.dongchimi.core.product.PreparedProductDraftCounts
import kr.dongchimi.core.product.PreparedProductSearchCondition
import kr.dongchimi.core.product.PreparedProductService
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

        fun newController(
            productService: ProductService = Mockito.mock(ProductService::class.java),
            preparedProductService: PreparedProductService = Mockito.mock(PreparedProductService::class.java),
        ): OwnerProductController = OwnerProductController(productService, OwnerPreparedProductDraftQueryFacade(preparedProductService))

        test("상세 조회 시 서비스 결과를 응답 data로 반환한다") {
            val productService = Mockito.mock(ProductService::class.java)
            Mockito.`when`(productService.getProduct(1L, marketId, 5L)).thenReturn(sampleProduct())
            val controller = newController(productService)

            val response = controller.getDetail(apiUser, marketId, 5L)

            response.success shouldBe true
            response.data!!.productId shouldBe 5L
            response.data!!.categoryName shouldBe "정육/달걀"
            Mockito.verify(productService).getProduct(1L, marketId, 5L)
        }

        test("삭제 시 userId·marketId·productId로 서비스를 호출한다") {
            val productService = Mockito.mock(ProductService::class.java)
            val controller = newController(productService)

            val response = controller.delete(apiUser, marketId, 5L)

            response.success shouldBe true
            Mockito.verify(productService).delete(1L, marketId, 5L)
        }

        test("일괄 삭제 시 검증된 productIds로 서비스를 호출한다") {
            val productService = Mockito.mock(ProductService::class.java)
            val controller = newController(productService)
            val request = ProductBulkDeleteRequest(productIds = listOf(1L, 2L, 3L))

            val response = controller.deleteAll(apiUser, marketId, request)

            response.success shouldBe true
            Mockito.verify(productService).deleteAll(1L, marketId, listOf(1L, 2L, 3L))
        }

        test("기간 일괄 수정 시 productIds와 할인 기간으로 서비스를 호출한다") {
            val productService = Mockito.mock(ProductService::class.java)
            val controller = newController(productService)
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
            val controller = newController(productService)
            val request = ProductResetRequest(dealType = DealType.DAILY)

            val response = controller.reset(apiUser, marketId, request)

            response.success shouldBe true
            Mockito.verify(productService).reset(1L, marketId, DealType.DAILY)
        }

        test("임시저장 목록 조회 시 응답 data에 카운트와 목록이 담긴다") {
            val preparedProductService = Mockito.mock(PreparedProductService::class.java)
            val condition = PreparedProductSearchCondition(search = null, categories = emptyList())
            val pageOffset = PageOffset(PageOffset.DEFAULT_PAGE, PageOffset.DEFAULT_SIZE)
            Mockito
                .`when`(preparedProductService.getDraftCounts(1L, marketId))
                .thenReturn(PreparedProductDraftCounts(totalCount = 1L, successCount = 1L, failCount = 0L))
            Mockito
                .`when`(preparedProductService.getDrafts(1L, marketId, condition, pageOffset))
                .thenReturn(listOf(samplePreparedProduct()))
            val controller = newController(preparedProductService = preparedProductService)

            val response =
                controller.getDrafts(apiUser, marketId, PreparedProductDraftSearchRequest(), PageOffsetRequest())

            response.success shouldBe true
            response.data!!.totalCount shouldBe 1L
            response.data!!.successCount shouldBe 1L
            response.data!!.failCount shouldBe 0L
            response.data!!.preparedProducts.map { it.preparedProductId } shouldBe listOf(1L)
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

private fun samplePreparedProduct(): PreparedProduct =
    PreparedProduct(
        id = 1L,
        marketId = 10L,
        name = "삼겹살 500g",
        thumbnailUrl = null,
        price = null,
        category = ProductCategory.MEAT_EGG,
        promotionalPhrase = null,
        discountPeriod = null,
        draftStatus = DraftStatus.SUCCESS,
        failReason = null,
    )
