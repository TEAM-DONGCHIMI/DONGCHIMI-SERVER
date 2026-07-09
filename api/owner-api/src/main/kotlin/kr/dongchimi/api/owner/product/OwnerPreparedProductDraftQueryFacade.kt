package kr.dongchimi.api.owner.product

import kr.dongchimi.api.owner.product.response.OwnerPreparedProductDraftListResponse
import kr.dongchimi.api.owner.product.response.OwnerPreparedProductDraftResponse
import kr.dongchimi.core.common.PageOffset
import kr.dongchimi.core.product.PreparedProductSearchCondition
import kr.dongchimi.core.product.PreparedProductService
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class OwnerPreparedProductDraftQueryFacade(
    private val preparedProductService: PreparedProductService,
) {
    @Transactional(readOnly = true)
    fun getDrafts(
        ownerId: Long,
        marketId: Long,
        condition: PreparedProductSearchCondition,
        pageOffset: PageOffset,
    ): OwnerPreparedProductDraftListResponse {
        val counts = preparedProductService.getDraftCounts(ownerId, marketId)
        val products = preparedProductService.getDrafts(ownerId, marketId, condition, pageOffset)

        return OwnerPreparedProductDraftListResponse(
            totalCount = counts.totalCount,
            successCount = counts.successCount,
            failCount = counts.failCount,
            preparedProducts = products.map { OwnerPreparedProductDraftResponse(it) },
        )
    }
}
