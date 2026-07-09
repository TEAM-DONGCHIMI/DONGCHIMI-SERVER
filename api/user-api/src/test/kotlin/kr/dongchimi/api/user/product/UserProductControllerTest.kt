package kr.dongchimi.api.user.product

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kr.dongchimi.api.user.UserApiUser
import kr.dongchimi.api.user.product.request.PeriodicProductListRequest
import kr.dongchimi.core.common.CursorSliceResult
import kr.dongchimi.core.product.DealType
import kr.dongchimi.core.product.DiscountPeriod
import kr.dongchimi.core.product.PeriodicProductSearchCondition
import kr.dongchimi.core.product.Price
import kr.dongchimi.core.product.Product
import kr.dongchimi.core.product.ProductCategory
import kr.dongchimi.core.product.ProductService
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import java.math.BigDecimal
import java.time.LocalDate

class UserProductControllerTest :
    FunSpec({
        val apiUser = UserApiUser(userId = 1L, roles = setOf("USER"))
        val marketId = 10L

        test("오늘의 특가 목록 조회 성공 시 totalCount와 매핑된 상품 목록을 반환한다") {
            val productService = Mockito.mock(ProductService::class.java)
            Mockito
                .`when`(productService.getAllActiveProducts(eqLong(marketId), eqDealType(DealType.DAILY), anyLocalDate()))
                .thenReturn(listOf(sampleProduct(1L), sampleProduct(2L)))
            val controller = UserProductController(productService)

            val response = controller.getDailyDeals(apiUser, marketId)

            response.success shouldBe true
            response.data!!.totalCount shouldBe 2
            response.data!!.products.map { it.productId } shouldBe listOf(1L, 2L)
            response.data!!
                .products
                .first()
                .discountRate shouldBe 20
        }

        test("오늘의 특가 상품이 없으면 totalCount 0, 빈 목록을 반환한다") {
            val productService = Mockito.mock(ProductService::class.java)
            Mockito
                .`when`(productService.getAllActiveProducts(eqLong(marketId), eqDealType(DealType.DAILY), anyLocalDate()))
                .thenReturn(emptyList())
            val controller = UserProductController(productService)

            val response = controller.getDailyDeals(apiUser, marketId)

            response.data!!.totalCount shouldBe 0
            response.data!!.products shouldBe emptyList()
        }

        test("행사 할인 상품 목록 조회 성공 시 content와 hasNext, nextCursor를 반환한다") {
            val productService = Mockito.mock(ProductService::class.java)
            val condition = PeriodicProductSearchCondition(category = null, cursor = null, size = 12)
            Mockito
                .`when`(
                    productService.getActiveProductsByCategory(eqLong(marketId), eqDealType(DealType.PERIODIC), eqCondition(condition)),
                ).thenReturn(
                    CursorSliceResult(
                        content = listOf(sampleProduct(1L, DealType.PERIODIC), sampleProduct(2L, DealType.PERIODIC)),
                        hasNext = true,
                        nextCursor = 2L,
                    ),
                )
            val controller = UserProductController(productService)

            val response = controller.getPeriodicDeals(apiUser, marketId, PeriodicProductListRequest())

            response.success shouldBe true
            response.data!!.content.map { it.productId } shouldBe listOf(1L, 2L)
            response.data!!.hasNext shouldBe true
            response.data!!.nextCursor shouldBe 2L
        }

        test("행사 할인 상품이 없으면 빈 목록과 hasNext false, nextCursor null을 반환한다") {
            val productService = Mockito.mock(ProductService::class.java)
            val condition = PeriodicProductSearchCondition(category = null, cursor = null, size = 12)
            Mockito
                .`when`(
                    productService.getActiveProductsByCategory(eqLong(marketId), eqDealType(DealType.PERIODIC), eqCondition(condition)),
                ).thenReturn(CursorSliceResult(content = emptyList(), hasNext = false, nextCursor = null))
            val controller = UserProductController(productService)

            val response = controller.getPeriodicDeals(apiUser, marketId, PeriodicProductListRequest())

            response.data!!.content shouldBe emptyList()
            response.data!!.hasNext shouldBe false
            response.data!!.nextCursor shouldBe null
        }
    })

// Kotlin 비널 파라미터에 Mockito 매처를 쓰기 위한 헬퍼 (매처는 null을 반환하므로 폴백을 준다)
private fun anyLocalDate(): LocalDate = Mockito.any(LocalDate::class.java) ?: LocalDate.now()

private fun eqLong(value: Long): Long = ArgumentMatchers.eq(value)

private fun eqDealType(value: DealType): DealType = ArgumentMatchers.eq(value) ?: value

private fun eqCondition(value: PeriodicProductSearchCondition): PeriodicProductSearchCondition = ArgumentMatchers.eq(value) ?: value

private fun sampleProduct(
    id: Long,
    dealType: DealType = DealType.DAILY,
) = Product(
    id = id,
    marketId = 10L,
    name = "풀무원 콩나물 500g",
    dealType = dealType,
    thumbnailUrl = "https://cdn.example.com/products/$id.png",
    price = Price(BigDecimal("5000"), BigDecimal("4000")),
    category = ProductCategory.VEGETABLE_FRUIT,
    promotionalPhrase = null,
    discountPeriod = DiscountPeriod(LocalDate.now(), LocalDate.now()),
)
