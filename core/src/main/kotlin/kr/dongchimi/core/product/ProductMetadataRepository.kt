package kr.dongchimi.core.product

interface ProductMetadataRepository {
    fun findById(id: Long): ProductMetadata?

    fun save(productMetadata: ProductMetadata): ProductMetadata

    /** 상품별 조회수를 원자적으로 증가시킨다. 행이 없으면 생성한다(upsert). */
    fun incrementViewCounts(deltas: Map<Long, Int>)
}
