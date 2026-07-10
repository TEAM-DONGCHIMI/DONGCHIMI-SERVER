package kr.dongchimi.core.product

import org.springframework.stereotype.Component

@Component
class ProductAppender(
    private val productRepository: ProductRepository,
) {
    fun append(product: Product): Product = productRepository.save(product)
}
