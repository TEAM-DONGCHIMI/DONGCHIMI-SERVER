package kr.dongchimi.api.owner.product

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kr.dongchimi.core.common.PageOffset
import kr.dongchimi.core.product.DraftStatus
import kr.dongchimi.core.product.PreparedProduct
import kr.dongchimi.core.product.PreparedProductDraftCounts
import kr.dongchimi.core.product.PreparedProductSearchCondition
import kr.dongchimi.core.product.PreparedProductService
import kr.dongchimi.core.product.ProductCategory
import org.mockito.Mockito

class OwnerPreparedProductDraftQueryFacadeTest :
    FunSpec({
        val ownerId = 1L
        val marketId = 10L
        val condition = PreparedProductSearchCondition(search = null, categories = emptyList())
        val pageOffset = PageOffset(PageOffset.DEFAULT_PAGE, PageOffset.DEFAULT_SIZE)

        test("두 서비스 메서드를 조합해 응답 DTO를 조립한다") {
            val preparedProductService = Mockito.mock(PreparedProductService::class.java)
            Mockito
                .`when`(preparedProductService.getDraftCounts(ownerId, marketId))
                .thenReturn(PreparedProductDraftCounts(totalCount = 2L, successCount = 1L, failCount = 1L))
            Mockito
                .`when`(preparedProductService.getDrafts(ownerId, marketId, condition, pageOffset))
                .thenReturn(listOf(samplePreparedProduct()))
            val facade = OwnerPreparedProductDraftQueryFacade(preparedProductService)

            val response = facade.getDrafts(ownerId, marketId, condition, pageOffset)

            response.totalCount shouldBe 2L
            response.successCount shouldBe 1L
            response.failCount shouldBe 1L
            response.preparedProducts.map { it.preparedProductId } shouldBe listOf(1L)
            Mockito.verify(preparedProductService).getDraftCounts(ownerId, marketId)
            Mockito.verify(preparedProductService).getDrafts(ownerId, marketId, condition, pageOffset)
        }
    })

private fun samplePreparedProduct(): PreparedProduct =
    PreparedProduct(
        id = 1L,
        marketId = 10L,
        name = "고등어",
        thumbnailUrl = null,
        price = null,
        category = ProductCategory.SEAFOOD,
        promotionalPhrase = null,
        discountPeriod = null,
        draftStatus = DraftStatus.SUCCESS,
        failReason = null,
    )
