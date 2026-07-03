package kr.dongchimi.db.product

import kr.dongchimi.core.product.Product
import kr.dongchimi.core.product.ProductRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@Repository
class ProductRepositoryImpl(
    private val productJpaRepository: ProductJpaRepository,
) : ProductRepository {
    override fun findById(id: Long): Product? = productJpaRepository.findByIdOrNull(id)?.toDomain()

    override fun save(product: Product): Product = productJpaRepository.save(ProductJpaEntity(product)).toDomain()
}
