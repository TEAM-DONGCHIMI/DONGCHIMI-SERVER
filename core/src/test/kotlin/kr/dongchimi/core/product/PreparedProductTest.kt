package kr.dongchimi.core.product

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.math.BigDecimal
import java.time.LocalDate

class PreparedProductTest :
    FunSpec({
        test("toProduct: 필수값이 모두 채워지면 상품으로 변환된다") {
            val product = complete().toProduct()

            product.name shouldBe "삼겹살 500g"
            product.dealType shouldBe DealType.PERIODIC
            product.price.discountedPrice shouldBe BigDecimal("4000")
        }
    })

private fun complete(): PreparedProduct =
    PreparedProduct(
        id = 1L,
        marketId = 10L,
        name = "삼겹살 500g",
        thumbnailUrl = "https://static.dongchimi.kr/test.png",
        price = Price(BigDecimal("5000"), BigDecimal("4000")),
        category = ProductCategory.MEAT_EGG,
        promotionalPhrase = "맛이 미쳤어요",
        discountPeriod = DiscountPeriod(LocalDate.of(2025, 8, 1), LocalDate.of(2025, 8, 16)),
        dealType = DealType.PERIODIC,
        draftStatus = DraftStatus.SUCCESS,
        failReason = null,
    )
