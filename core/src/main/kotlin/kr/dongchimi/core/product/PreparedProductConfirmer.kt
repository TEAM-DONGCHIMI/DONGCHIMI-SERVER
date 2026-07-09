package kr.dongchimi.core.product

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class PreparedProductConfirmer(
    private val productRepository: ProductRepository,
    private val preparedProductRepository: PreparedProductRepository,
) {
    @Transactional
    fun confirm(drafts: List<PreparedProduct>) {
        productRepository.saveAll(drafts.map { it.toProduct() })
        preparedProductRepository.softDeleteByIds(drafts.map { it.id })
    }
}
