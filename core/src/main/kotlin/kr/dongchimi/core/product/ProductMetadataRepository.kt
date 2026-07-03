package kr.dongchimi.core.product

interface ProductMetadataRepository {
    fun findById(id: Long): ProductMetadata?

    fun save(productMetadata: ProductMetadata): ProductMetadata
}
