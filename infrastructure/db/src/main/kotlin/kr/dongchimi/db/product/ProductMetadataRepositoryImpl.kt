package kr.dongchimi.db.product

import kr.dongchimi.core.product.ProductMetadata
import kr.dongchimi.core.product.ProductMetadataRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class ProductMetadataRepositoryImpl(
    private val productMetadataJpaRepository: ProductMetadataJpaRepository,
) : ProductMetadataRepository {
    override fun findById(id: Long): ProductMetadata? = productMetadataJpaRepository.findByIdOrNull(id)?.toDomain()

    override fun save(productMetadata: ProductMetadata): ProductMetadata =
        productMetadataJpaRepository.save(ProductMetadataJpaEntity(productMetadata)).toDomain()

    @Transactional
    override fun incrementViewCounts(deltas: Map<Long, Int>) {
        // id 오름차순으로 적용해 동시 flush 간 잠금 획득 순서를 일관되게 → 교차 데드락 예방.
        deltas.toSortedMap().forEach { (id, delta) -> productMetadataJpaRepository.upsertIncrement(id, delta) }
    }
}
