package kr.dongchimi.core.product

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class PreparedProductUpdater(
    private val preparedProductRepository: PreparedProductRepository,
    private val draftFailReasonResolver: DraftFailReasonResolver,
) {
    /**
     * 요청 바디를 마켓 임시저장 목록의 최종 상태로 본다. 바디에 없는 기존 임시저장 상품은 삭제하고,
     * 바디에 있는 상품은 갱신한다. 삭제와 갱신을 한 트랜잭션으로 묶는다.
     */
    @Transactional
    fun syncDrafts(
        marketId: Long,
        commands: List<PreparedProductDraftSaveCommand>,
    ) {
        val requestIds = commands.map { it.id }.toSet()
        val idsToDelete = preparedProductRepository.findAllIdsByMarketId(marketId).filter { it !in requestIds }

        preparedProductRepository.softDeleteByIds(idsToDelete)
        preparedProductRepository.updateDrafts(commands, draftFailReasonResolver.resolve(commands))
    }
}
