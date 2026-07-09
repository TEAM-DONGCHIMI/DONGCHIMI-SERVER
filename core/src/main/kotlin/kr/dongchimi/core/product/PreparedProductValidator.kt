package kr.dongchimi.core.product

import kr.dongchimi.core.common.exception.CoreException
import org.springframework.stereotype.Component

@Component
class PreparedProductValidator(
    private val preparedProductRepository: PreparedProductRepository,
) {
    fun validateAllInMarket(
        ids: List<Long>,
        marketId: Long,
    ) {
        if (preparedProductRepository.countInMarket(ids, marketId) != ids.size) {
            throw CoreException(PreparedProductErrorCode.PREPARED_PRODUCT_NOT_FOUND)
        }
    }

    fun validateAllCompleted(drafts: List<PreparedProduct>) {
        if (drafts.any { it.draftStatus == DraftStatus.FAIL }) {
            throw CoreException(PreparedProductErrorCode.DRAFT_NOT_COMPLETED)
        }
    }
}
