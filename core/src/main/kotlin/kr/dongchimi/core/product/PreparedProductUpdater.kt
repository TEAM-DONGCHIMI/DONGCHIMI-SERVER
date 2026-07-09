package kr.dongchimi.core.product

import org.springframework.stereotype.Component

@Component
class PreparedProductUpdater(
    private val preparedProductRepository: PreparedProductRepository,
    private val draftFailReasonResolver: DraftFailReasonResolver,
) {
    fun updateDrafts(commands: List<PreparedProductDraftSaveCommand>) {
        preparedProductRepository.updateDrafts(commands, draftFailReasonResolver.resolve(commands))
    }
}
