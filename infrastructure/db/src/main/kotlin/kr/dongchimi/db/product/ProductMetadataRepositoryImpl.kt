package kr.dongchimi.db.product

import kr.dongchimi.core.product.ProductMetadata
import kr.dongchimi.core.product.ProductMetadataRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@Repository
class ProductMetadataRepositoryImpl(
    private val productMetadataJpaRepository: ProductMetadataJpaRepository,
) : ProductMetadataRepository {
    override fun findById(id: Long): ProductMetadata? = productMetadataJpaRepository.findByIdOrNull(id)?.toDomain()

    override fun save(productMetadata: ProductMetadata): ProductMetadata =
        productMetadataJpaRepository.save(ProductMetadataJpaEntity(productMetadata)).toDomain()
}
