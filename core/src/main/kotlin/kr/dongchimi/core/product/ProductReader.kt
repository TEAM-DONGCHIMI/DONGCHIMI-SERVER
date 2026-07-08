package kr.dongchimi.core.product

import kr.dongchimi.core.common.exception.CoreException
import org.springframework.stereotype.Component

@Component
class ProductReader(
    private val productRepository: ProductRepository,
) {
    fun read(id: Long): Product = productRepository.findById(id) ?: throw CoreException(ProductErrorCode.PRODUCT_NOT_FOUND)
}
