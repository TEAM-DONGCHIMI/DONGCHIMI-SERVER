package kr.dongchimi.db.admin

import kr.dongchimi.core.admin.DefaultProductThumbnail
import kr.dongchimi.core.admin.DefaultProductThumbnailRepository
import kr.dongchimi.core.product.ProductCategory
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@Repository
class DefaultProductThumbnailRepositoryImpl(
    private val defaultProductThumbnailJpaRepository: DefaultProductThumbnailJpaRepository,
) : DefaultProductThumbnailRepository {
    override fun findById(id: Long): DefaultProductThumbnail? = defaultProductThumbnailJpaRepository.findByIdOrNull(id)?.toDomain()

    override fun findAllByCategoryIn(categories: Set<ProductCategory>): List<DefaultProductThumbnail> =
        defaultProductThumbnailJpaRepository.findAllByCategoryIn(categories).map { it.toDomain() }

    override fun findAllByLatest(
        search: String?,
        cursor: Long?,
        limit: Int,
    ): List<DefaultProductThumbnail> =
        defaultProductThumbnailJpaRepository
            .findAllByLatest(cursor, search, PageRequest.of(0, limit))
            .map { it.toDomain() }

    override fun findAllByName(
        search: String?,
        cursor: Long?,
        cursorName: String?,
        limit: Int,
    ): List<DefaultProductThumbnail> =
        defaultProductThumbnailJpaRepository
            .findAllByName(cursor, cursorName, search, PageRequest.of(0, limit))
            .map { it.toDomain() }

    override fun findNameById(id: Long): String? = defaultProductThumbnailJpaRepository.findNameById(id)

    override fun save(defaultProductThumbnail: DefaultProductThumbnail): DefaultProductThumbnail =
        defaultProductThumbnailJpaRepository.save(DefaultProductThumbnailJpaEntity(defaultProductThumbnail)).toDomain()
}
